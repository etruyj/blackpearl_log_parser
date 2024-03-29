//===================================================================
// GetJobIDtoChunkMap.java
// 	Description:
// 		Calls all the dataplanner-main.logs to get a map of
// 		all the job IDs to chunks.
//===================================================================

package com.socialvagrancy.blackpearl.logs.utils.importers;

import com.socialvagrancy.blackpearl.logs.utils.inputs.LogReader;
import com.socialvagrancy.blackpearl.logs.utils.parsers.JobIDtoChunkParser;
import com.socialvagrancy.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class GetJobIDtoChunkMap
{
	public static void main(String[] args)
	{
		GetJobIDtoChunkMap.fromDataplanner(args[0], 8, null, true);
	}

	public static HashMap<String, ArrayList<String>> fromDataplanner(String dir_path, int log_count, Logger log, boolean debugging)
	{
		ArrayList<String> exception_list = new ArrayList<String>();

		System.err.print("Importing chunk info...\t\t");

		JobIDtoChunkParser parser = new JobIDtoChunkParser();
		HashMap<String, ArrayList<String>> id_map;
		String log_name = "logs/var.log.dataplanner-main.log";
		String file_name;
		
		for(int i=log_count; i>=0; i--)
		{
			try
			{
				if(i>0)
				{
					file_name = log_name + "." + i;
				}
				else
				{
					file_name = log_name;
				}

				if(debugging)
				{
					System.err.print("\n"); // Close header line: importing...
					System.err.println(dir_path + file_name);
				}

				LogReader.readLog(dir_path + file_name, parser, null);
			}
			catch(Exception e)
			{
				// For cleaner output, collect exceptions and output them
				// after the complete is printed.
				exception_list.add(e.getMessage());
			}
		}

		id_map = parser.getIDMap();
	
		System.err.println("[COMPLETE]");

		for(int i=0; i<exception_list.size(); i++)
		{
			System.err.println(exception_list.get(i));
		}

		return id_map;
		
	}	

	public static void print(HashMap<String, ArrayList<String>> id_map)
	{
		for(String key : id_map.keySet())
		{
			System.out.println(key);

			for(int j=0; j<id_map.get(key).size(); j++)
			{
				System.out.println("\t- " + id_map.get(key).get(j));
			}
		}
	}
}
