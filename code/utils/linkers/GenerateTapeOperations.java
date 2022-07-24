//===================================================================
// GenerateTapeOperation.java
// 	Description:
// 		Scans the dataplanner and tape_backend logs and then
// 		creates a consolidated variable of a complete write
// 		to tape process from tape task to completion of writing
// 		to tape.
//===================================================================

package com.socialvagrancy.blackpearl.logs.utils.linkers;

import com.socialvagrancy.blackpearl.logs.structures.TapeActivity;
import com.socialvagrancy.blackpearl.logs.structures.TapeExchange;
import com.socialvagrancy.blackpearl.logs.structures.TapeJob;
import com.socialvagrancy.blackpearl.logs.structures.Task;
import com.socialvagrancy.blackpearl.logs.structures.operations.TapeOperation;
import com.socialvagrancy.blackpearl.logs.utils.importers.GetTapeExchanges;
import com.socialvagrancy.blackpearl.logs.utils.importers.GetTapeJobs;
import com.socialvagrancy.blackpearl.logs.utils.importers.GetTapeTasks;
import com.socialvagrancy.blackpearl.logs.utils.BPLogDateConverter;
import com.socialvagrancy.utils.Logger;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class GenerateTapeOperations
{

	public static ArrayList<TapeOperation> fromLogs(String dir_path, int dataplanner_log_count, int tape_backend_log_count, Logger log)
	{
		// The dataplanner and tape_backend logs are stitched together.
		// The dataplanner task completed (date_completed) timestamp is used to
		// limit the tape job.
		// Once the tape job is located. The exchange that put that tape in the drive
		// can be determined.

		ArrayList<TapeOperation> ops_list = populateTasks(dir_path, dataplanner_log_count, log);
		ops_list = populateTapeJobs(ops_list, dir_path, tape_backend_log_count, log);
		ops_list = populateTapeExchanges(ops_list, dir_path, tape_backend_log_count, log);

		return ops_list;
	}

	private static ArrayList<TapeOperation> populateTapeExchanges(ArrayList<TapeOperation> ops_list, String dir_path, int tape_backend_log_count, Logger log)
	{
		int exch_counter = 0;

		ArrayList<TapeExchange> exch_list = GetTapeExchanges.fromTapeBackend(dir_path, tape_backend_log_count, log);
		
		HashMap<String, TreeMap<LocalDateTime, TapeExchange>> exchange_map = MapTapeActivities.mapActivity(exch_list);
		LocalDateTime start_time;
		LocalDateTime data_start_time;
		LocalDateTime mount_time;
		LocalDateTime eject_time;
		DateTimeFormatter final_format = DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss");
		TapeExchange exchange;

		for(int i=0; i<ops_list.size(); i++)
		{
			// Create datetime variables for the boundary times
			//	This allows for searching the treemap for the closest timestamp
			//	Using LocalDateTime as the date is already formatted.
			start_time = LocalDateTime.parse(ops_list.get(i).task_created, final_format);	
			data_start_time = LocalDateTime.parse(ops_list.get(i).rw_start_time, final_format);

			// Subtract 1 second from start_time as it is possible a move will be queued the same second
			// a job is created. This corrects an issue where tape exchanges queued immediately after the
			// job is created are being missed with the TreeMap.afterKey(time) search.
			start_time = start_time.minusSeconds(1);

			// Find the tape mount that started before data started being written to the drive.
			// As data can't be written to an empty drive, the assumption is this move has to be
			// a tape mount.
			// It's possible a mount_time doesn't exist in the logs. If null, set the date to Jan 1, 2000.
			// This will cause it to fail the test and have the drive marked as already loaded.
			mount_time = exchange_map.get(ops_list.get(i).drive_wwn).lowerKey(data_start_time);

			if(mount_time == null)
			{
				// An exchange is needed to fill out the drive and tape info
				mount_time = exchange_map.get(ops_list.get(i).drive_wwn).higherKey(data_start_time);
				exchange = exchange_map.get(ops_list.get(i).drive_wwn).get(mount_time);

				// Set mount time to a pre-blackpearl era
				// This gives us a value for doing other tasks and lets us mark the exchange
				// as occuring before the task was created, i.e. the drive was already loaded.
				mount_time = LocalDateTime.parse("2000 JAN 01 00:00:01", final_format);
				eject_time = LocalDateTime.parse("2000 JAN 01 00:00:01", final_format);
			}
			else
			{
				// Ejection should be the move before the exchange identified above. Tape can't be
				// moved to a drive that is already occupied, so this has to be the eject call.
				eject_time = exchange_map.get(ops_list.get(i).drive_wwn).lowerKey(mount_time);
				exchange = exchange_map.get(ops_list.get(i).drive_wwn).get(mount_time);
			}

			// Identify the drive number
			/*
			 * THIS WILL BE A FUNCTION CALL ADDED IN THE FUTURE
			 * STACKS AND BOAS HAVE DIFFERENT CONVERSIONS
			 */

			ops_list.get(i).barcode = exchange.tape_barcode;

			if(mount_time.isAfter(start_time))
			{
				// If the move occurred after the start time, it was the result of the job.
				ops_list.get(i).already_in_drive = false;
			       	ops_list.get(i).mount_start = BPLogDateConverter.formatTapeBackendTimestamp(exchange.start_time);
				ops_list.get(i).mount_end = BPLogDateConverter.formatTapeBackendTimestamp(exchange.end_time);
					
			}
			else
			{
				ops_list.get(i).already_in_drive = false;
			       	ops_list.get(i).mount_start = "2000 JAN 01 00:00:01";
				ops_list.get(i).mount_end = "2000 JAN 01 00:00:01";
			}
		}

		return ops_list;
	}

	private static ArrayList<TapeOperation> populateTapeJobs(ArrayList<TapeOperation> ops_list, String dir_path, int tape_backend_log_count, Logger log)
	{
		ArrayList<TapeJob> job_list = GetTapeJobs.fromTapeBackend(dir_path, tape_backend_log_count, log);
		HashMap<String, TreeMap<LocalDateTime, TapeJob>> job_map = MapTapeActivities.mapActivity(job_list);

		// Time vars are created to easily reference
		// fields within the different list and to 
		// make the code easier to follow.
		LocalDateTime start_time;
		LocalDateTime end_time;
		LocalDateTime job_time;
		LocalDateTime job_end_time;
		LocalDateTime test_time;
		String time_converter; // Holds the string value of the converted time.
		DateTimeFormatter final_format = DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss");
		TapeJob job;

		for(int i=0; i<ops_list.size(); i++)
		{
			start_time = LocalDateTime.parse(ops_list.get(i).task_created, final_format);
			end_time = LocalDateTime.parse(ops_list.get(i).task_completed, final_format);

			test_time = end_time;

			do
			{
				// Find the job time that occured for the ops_list.drive_wwn 
				// just before the test time.
				job_time = job_map.get(ops_list.get(i).drive_wwn).lowerKey(test_time);

				// Store the job that occured at that time.
				job = job_map.get(ops_list.get(i).drive_wwn).get(job_time);
				
				time_converter = BPLogDateConverter.formatTapeBackendTimestamp(job.end_time);
				job_end_time = LocalDateTime.parse(time_converter, final_format);

				test_time = job_time; // Set this in case we hit the while condition
			} while(job_end_time.isAfter(end_time));

			ops_list.get(i).rw_start_time = BPLogDateConverter.formatTapeBackendTimestamp(job.start_time);
			ops_list.get(i).rw_end_time = BPLogDateConverter.formatTapeBackendTimestamp(job.end_time);
			ops_list.get(i).rw_duration = job.duration;
			ops_list.get(i).blob = job.blob;
		}

		return ops_list;
	}	

	private static ArrayList<TapeOperation> populateTasks(String dir_path, int dataplanner_log_count, Logger log)
	{
		ArrayList<TapeOperation> ops_list = new ArrayList<TapeOperation>();
		TapeOperation op;
		
		ArrayList<Task> task_list = GetTapeTasks.fromDataplanner(dir_path, dataplanner_log_count, log);

		for(int i=0; i<task_list.size(); i++)
		{
			// iterate through the copies as multiple storage domains
			// can be written by the same tape task.
			for(int j=0; j<=task_list.get(i).copies; j++)
			{
				op = new TapeOperation();
			
				op.id = task_list.get(i).id;
				op.request_type = task_list.get(i).type;
				op.chunk_id = task_list.get(i).chunk_id;
				op.drive_wwn = task_list.get(i).sd_copy.get(j).target;
				op.task_created = BPLogDateConverter.formatDataPlannerTimestamp(task_list.get(i).sd_copy.get(j).created_at);
				op.task_completed = BPLogDateConverter.formatDataPlannerTimestamp(task_list.get(i).sd_copy.get(j).date_completed);
				op.task_size = task_list.get(i).sd_copy.get(j).size;
				op.task_throughput = task_list.get(i).sd_copy.get(j).throughput;

				ops_list.add(op);
			}
		}

		log.INFO("Added (" + ops_list.size() + ") to ops_list.");

		return ops_list;
	}
}

