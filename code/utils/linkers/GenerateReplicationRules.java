//===================================================================
// GenerateReplicatonRules.java
// 	Description:
// 		Joins the replication rule with the target information
// 		to give a class that translates the information for 
// 		the full replication action.
//===================================================================

package com.socialvagrancy.blackpearl.logs.utils.linkers;

import com.socialvagrancy.blackpearl.logs.structures.outputs.ReplicationRule;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDS3RepRules;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDS3RepTargets;

import java.util.ArrayList;
import java.util.HashMap;

public class GenerateReplicationRules
{
	public static ArrayList<ReplicationRule> ds3Rules(GuiDS3RepRules rules, GuiDS3RepTargets targets)
	{
		ArrayList<ReplicationRule> rules_list = new ArrayList<ReplicationRule>();
		HashMap<String, Integer> target_id_map = MapDS3TargetToID.createMap(targets);
		
		ReplicationRule rule;

		for(int i=0; i < rules.ruleCount(); i++)
		{
			rule = new ReplicationRule();

			rule.id = rules.id(i);
			rule.type = "DS3";
			rule.data_policy_id = rules.dataPolicyID(i);
			rule.target_id = rules.targetID(i);
			
			if(target_id_map.get(rule.target_id) != null)
			{
				rule.target_name = targets.name(target_id_map.get(rule.target_id));
				rule.target_ip = targets.dataPath(target_id_map.get(rule.target_id));
			}
			else
			{
				rule.target_name = "UNKNOWN";
			}		


			rules_list.add(rule);
		}

		return rules_list;
	}
}
