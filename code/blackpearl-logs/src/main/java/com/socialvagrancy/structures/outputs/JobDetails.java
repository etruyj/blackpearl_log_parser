//===================================================================
// JobDetails.java
// 	Description:
// 		This holds the final detailed information for the
// 		assembled job.
//===================================================================

package com.socialvagrancy.blackpearl.logs.structures.outputs;

import com.socialvagrancy.blackpearl.logs.structures.operations.DS3Operation;
import com.socialvagrancy.blackpearl.logs.structures.operations.PoolOperation;
import com.socialvagrancy.blackpearl.logs.structures.operations.TapeOperation;
import com.socialvagrancy.blackpearl.logs.structures.CompletedJob;

import java.util.ArrayList;
import java.util.HashMap;

public class JobDetails
{
	public CompletedJob.JobInfo job_info;
	public HashMap<String, ArrayList<TapeOperation>> tape_copies;
	public HashMap<String, ArrayList<PoolOperation>> pool_copies;
	public HashMap<String, ArrayList<DS3Operation>> ds3_copies;

	//=======================================
	// Calculated job values.
	//=======================================

	public String human_readable_size;
	public String job_duration;
	public String bucket = "[ADD THIS]";

	public JobDetails()
	{
		tape_copies = new HashMap<String, ArrayList<TapeOperation>>();
		pool_copies = new HashMap<String, ArrayList<PoolOperation>>();
		ds3_copies = new HashMap<String, ArrayList<DS3Operation>>();
	}

	//=======================================
	// Getters
	//=======================================

	public String bucketID() { return job_info.bucket_id; }
	public String cachedSize() { return job_info.cached_size_in_bytes; }
	public String completedSize() { return job_info.completed_size_in_bytes; }
	public String createdAt() { return job_info.created_at; }
	public String id() { return job_info.id; }
	public String originalSize() { return job_info.original_size_in_bytes; }
	public String owner() { return job_info.user_username; }
	public String name() { return job_info.name; }
	public String type() { return job_info.request_type; }

	// Total copies
	public int ds3CopyCount() { return ds3_copies.size(); }
	public int poolCopyCount() { return pool_copies.size(); }
	public int tapeCopyCount() { return tape_copies.size();	}



	// Copies by Chunk
	public int tapeCopyCount(String chunk) 
	{
	       	if(tape_copies.get(chunk) == null)
		{
			return 0;
		}
		else
		{
			return tape_copies.get(chunk).size(); 
		}
	}

	public int poolCopyCount(String chunk) 
	{ 
		if(pool_copies.get(chunk) == null)
		{
			return 0;
		}
		else
		{
			return pool_copies.get(chunk).size(); 
		}
	}

	public int ds3CopyCount(String chunk)
	{
		if(ds3_copies.get(chunk) == null)
		{
			return 0;
		}
		else
		{
			return ds3_copies.get(chunk).size();
		}
	}

	// DS3 Getters
	public String ds3Name(String chunk, int i) { return ds3_copies.get(chunk).get(i).target_name; }
	public String ds3TaskDuration(String chunk, int i) { return ds3_copies.get(chunk).get(i).duration; }
	public String ds3TaskEnd(String chunk, int i) { return ds3_copies.get(chunk).get(i).date_completed; }
	public String ds3TaskID(String chunk, int i) { return ds3_copies.get(chunk).get(i).id; }
	public String ds3TaskSize(String chunk, int i) { return ds3_copies.get(chunk).get(i).size; }
	public String ds3TaskStart(String chunk, int i) { return ds3_copies.get(chunk).get(i).created_at; }
	public String ds3TaskThroughput(String chunk, int i) { return ds3_copies.get(chunk).get(i).throughput; }
	//public String ds3IP(String chunk, int i) { return ds3_copies.get(chunk).get

	// Pool Getters
	public String poolName(String chunk, int i) { return pool_copies.get(chunk).get(i).pool_name; }
	public String poolTaskDuration(String chunk, int i) { return pool_copies.get(chunk).get(i).duration; }
	public String poolTaskEnd(String chunk, int i) { return pool_copies.get(chunk).get(i).date_completed; }
	public String poolTaskID(String chunk, int i) { return pool_copies.get(chunk).get(i).task_id; }
	public String poolTaskSize(String chunk, int i) { return pool_copies.get(chunk).get(i).size; }
	public String poolTaskStart(String chunk, int i) { return pool_copies.get(chunk).get(i).created_at; }
	public String poolTaskThroughput(String chunk, int i) { return pool_copies.get(chunk).get(i).throughput; }
	
	// Tape Getters
	public String tapeBarcode(String chunk, int i) { return tape_copies.get(chunk).get(i).barcode; }
	public String tapeDataDuration(String chunk, int i) { return tape_copies.get(chunk).get(i).rw_duration; } 
	public String tapeDataEnd(String chunk, int i) { return tape_copies.get(chunk).get(i).rw_end_time; } 
	public String tapeDataStart(String chunk, int i) { return tape_copies.get(chunk).get(i).rw_start_time; }
       	public String tapeDriveNumber(String chunk, int i) { return tape_copies.get(chunk).get(i).drive_number; }	
	public String tapeDriveSN(String chunk, int i) { return tape_copies.get(chunk).get(i).drive_wwn; }
	public String tapeInDrive(String chunk, int i)
	{
		if(tape_copies.get(chunk).get(i).already_in_drive)
		{
			return "TRUE";
		}
		else
		{
			return "FALSE";
		}
	}

	public String tapeMountDuration(String chunk, int i) { return tape_copies.get(chunk).get(i).mount_duration; } 
	public String tapeMountEnd(String chunk, int i) { return tape_copies.get(chunk).get(i).mount_end; } 
	public String tapeMountStart(String chunk, int i) { return tape_copies.get(chunk).get(i).mount_start; } 
	public String tapePartition(String chunk, int i) { return tape_copies.get(chunk).get(i).partition_name; }
	public String tapeTaskDuration(String chunk, int i) { return tape_copies.get(chunk).get(i).task_duration; }
	public String tapeTaskEnd(String chunk, int i) { return tape_copies.get(chunk).get(i).task_completed; }
	public String tapeTaskID(String chunk, int i) { return tape_copies.get(chunk).get(i).id; }
	public String tapeTaskSize(String chunk, int i) { return tape_copies.get(chunk).get(i).task_size; }
	public String tapeTaskStart(String chunk, int i) { return tape_copies.get(chunk).get(i).task_created; }
	public String tapeTaskThroughput(String chunk, int i) { return tape_copies.get(chunk).get(i).task_throughput; }

	//=======================================
	// Settings
	//=======================================
	
	public void addPoolInfo(String chunk, ArrayList<PoolOperation> ops_list) { pool_copies.put(chunk, ops_list); }
	public void addDS3Info(String chunk, ArrayList<DS3Operation> ops_list) { ds3_copies.put(chunk, ops_list); }
}
