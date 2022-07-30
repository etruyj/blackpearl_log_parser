//===================================================================
// LinkJobToDS3Operations.java
// 	Description:
// 		Populates ArrayList<JobDetails> with DS3Operation info.
//===================================================================

package com.socialvagrancy.blackpearl.logs.utils.linkers;

import com.socialvagrancy.blackpearl.logs.structures.CompletedJob;
import com.socialvagrancy.blackpearl.logs.structures.outputs.JobDetails;
import com.socialvagrancy.blackpearl.logs.structures.operations.DS3Operation;

import java.util.ArrayList;
import java.util.HashMap;

public class LinkJobToDS3Operations
{
	public static ArrayList<JobDetails> addDS3(ArrayList<JobDetails> details_list, HashMap<String, ArrayList<String>> id_chunk_map, ArrayList<DS3Operation> ops_list)
	{
		HashMap<String, ArrayList<DS3Operation>> ops_map = MapDS3OperationsToChunk.createMap(ops_list);
		ArrayList<String> chunks;

		System.err.print("\n");
		System.err.println("DS3 list size: " + ops_list.size());
		System.err.println("DS3 map size: " + ops_map.size());
		System.err.print("\n");

		for(int i=0; i < details_list.size(); i++)
		{
			chunks = id_chunk_map.get(details_list.get(i).job_info.id);

			if(chunks != null)
			{
				for(int j=0; j < chunks.size(); j++)
				{
					if(ops_map.get(chunks.get(j)) != null)
					{
						details_list.get(i).addDS3Info(chunks.get(j), ops_map.get(chunks.get(j)));
					}
					else
					{
						System.err.println("No DS3 replication tasks exist for job [" + details_list.get(i).job_info.name + "] created at " 
								+ details_list.get(i).job_info.created_at);
					}
				}
			}
		}

		return details_list;
	}
}
