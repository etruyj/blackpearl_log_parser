//===================================================================
// Controller.java
// 	Description: This class calls the individual commands to allow
// 	easier management of the interface.
//===================================================================

package com.socialvagrancy.blackpearl.logs.ui;

import com.socialvagrancy.blackpearl.logs.commands.CalcJobStatistics;
import com.socialvagrancy.blackpearl.logs.commands.CalcJobStats;
import com.socialvagrancy.blackpearl.logs.structures.JobStatistics;

import java.util.ArrayList;

public class Controller
{
	public static ArrayList<JobStatistics> jobStatistics(String path)
	{
		CalcJobStats.forCompletedJobs(path, null);
	//	ArrayList<JobStatistics> stat_list = CalcJobStats.fromCompletedJobs(path, null);
		
	//	CalcJobStatistics.print(stat_list);

		return null;
	}
}
