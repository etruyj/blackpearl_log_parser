//===================================================================
// GenerateDataPolicy.java
// 	Description:
// 		Takes the Data Policy and Data Persistence Rule information
// 		along with an ArrayList<StorageDomain>, which is the
// 		output of the commands/ListStorageDomains.java in order
// 		to build a full picture of the data policy from the
// 		distinct parts.
//===================================================================

package com.socialvagrancy.blackpearl.logs.utils.linkers;

import com.socialvagrancy.blackpearl.logs.structures.outputs.DataPolicy;
import com.socialvagrancy.blackpearl.logs.structures.outputs.ReplicationRule;
import com.socialvagrancy.blackpearl.logs.structures.outputs.StorageDomain;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDataPolicy;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDataPersistenceRules;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiAzureRepRules;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiAzureRepTargets;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDS3RepRules;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiDS3RepTargets;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiS3RepRules;
import com.socialvagrancy.blackpearl.logs.structures.rest.GuiS3Targets;

import java.util.ArrayList;
import java.util.HashMap;

public class GenerateDataPolicy
{
	public static ArrayList<DataPolicy> fromRest(GuiDataPolicy policies, /*HashMap<String, ArrayList<String>> dp_to_sd_id_map,*/ GuiDataPersistenceRules persistence_rules, ArrayList<StorageDomain> domains_list)
	{
		ArrayList<DataPolicy> policy_list = new ArrayList<DataPolicy>();
		HashMap<String, GuiDataPersistenceRules.PersistenceRule> rule_id_map = MapPersistenceRuleToID.createMap(persistence_rules);
		HashMap<String, ArrayList<String>> dp_to_pr_id_map = MapDataPolicyIDtoPersistenceRuleIDs.createMap(persistence_rules);
		HashMap<String, StorageDomain> domain_map = MapStorageDomainToID.createMap(domains_list);
		DataPolicy data_policy;
		String storage_domain_id;

		for(int i=0; i < policies.policyCount(); i++)
		{
			data_policy = new DataPolicy();

			data_policy.name = policies.getName(i);
			data_policy.id = policies.getID(i);
			data_policy.blobbing_enabled = policies.blobbing(i);
			data_policy.minimize_spanning = policies.spanning(i);
			data_policy.default_get_priority = policies.priorityGet(i);
			data_policy.default_put_priority = policies.priorityPut(i);
			data_policy.default_verify_priority = policies.priorityVerify(i);
			data_policy.rebuild_priority = policies.priorityRebuild(i);
			data_policy.checksum = policies.checksumType(i);
			data_policy.end_to_end_crc = policies.endToEndCRC(i);
			data_policy.versioning = policies.versioning(i);
			data_policy.versions_to_keep = policies.versionsToKeep(i);

			// Quick check for null values
			// It is possible for data policies not to have persistence rules assigned.

			if(dp_to_pr_id_map.get(data_policy.id) != null)
			{
				String pr_id; // persistence rule id;
				String sd_id; // storage domain id
	
				for(int j=0; j < dp_to_pr_id_map.get(data_policy.id).size(); j++)
				{
					pr_id = dp_to_pr_id_map.get(data_policy.id).get(j);
				
					if(rule_id_map.get(pr_id) == null)
					{
						System.err.println("WARN: unable to find persistence rule with id " + pr_id);
					}
					else
					{	
						GuiDataPersistenceRules.PersistenceRule rule = rule_id_map.get(pr_id);

						sd_id = rule.storage_domain_id;

						data_policy.addPersistenceRule(domain_map.get(sd_id), rule.type,
								rule.state, rule.isolation_level, rule.minimum_days_to_retain);	
					}
				}
			}
			else
			{
				System.err.println("WARN: No data persistence rules found for data policy " + data_policy.name);
			}

			policy_list.add(data_policy);
		}

		return policy_list;
	}
	
	public static ArrayList<DataPolicy> withReplication(GuiDataPolicy policies, /*HashMap<String, ArrayList<String>> dp_to_sd_id_map,*/ GuiDataPersistenceRules persistence_rules, ArrayList<StorageDomain> domains_list, GuiDS3RepRules ds3_rep_rules, GuiDS3RepTargets ds3_targets, GuiS3RepRules s3_rep_rules, GuiS3Targets s3_rep_targets, GuiAzureRepRules azure_rep_rules, GuiAzureRepTargets azure_rep_targets)
	{
		// The replication rule free version was coded first. To avoid
		// rejiggering it, this function was put just above it.
		ArrayList<DataPolicy> policy_list = fromRest(policies, persistence_rules, domains_list);
		
		HashMap<String, Integer> policy_map = MapDataPolicyToID.createIndexMap(policy_list);

		ArrayList<ReplicationRule> ds3_reps = GenerateReplicationRules.ds3Rules(ds3_rep_rules, ds3_targets);
		ds3_reps.addAll(GenerateReplicationRules.s3Rules(s3_rep_rules, s3_rep_targets));
		ds3_reps.addAll(GenerateReplicationRules.azureRules(azure_rep_rules, azure_rep_targets));

		// Attach DS3 Replication Rules
		int index;

		for(int i=0; i < ds3_reps.size(); i++)
		{
			if(policy_map.get(ds3_reps.get(i).data_policy_id) != null)
			{
				index = policy_map.get(ds3_reps.get(i).data_policy_id);

				policy_list.get(index).addReplicationRule(ds3_reps.get(i));
			}
		}

		return policy_list;
	}
}
