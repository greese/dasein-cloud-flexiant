/**
 * Copyright (C) 2012-2013 Dell, Inc.
 * See annotations for authorship information
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.flexiant.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.network.AbstractFirewallSupport;
import org.dasein.cloud.network.Direction;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallCreateOptions;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.Permission;
import org.dasein.cloud.network.RuleTargetType;

import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.FirewallRuleAction;
import com.extl.jade.user.FirewallTemplate;
import com.extl.jade.user.IpType;
import com.extl.jade.user.Job;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.ResourceKey;
import com.extl.jade.user.ResourceKeyType;
import com.extl.jade.user.ResourceMetadata;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserService;

/**
 * The AbstractFirewallSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOFirewallSupport extends AbstractFirewallSupport {

	UserService userService;

	public FCOFirewallSupport(FCOProvider provider) {
		super(provider);

		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public void delete(String firewallId) throws InternalException, CloudException {
		try{
			userService.deleteResource(firewallId, true, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public String getProviderTermForFirewall(Locale locale) {
		return "Firewall Template";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean supportsFirewallCreation(boolean inVlan) throws CloudException, InternalException {
		if(inVlan){
			return false;
		}
		
		return true;
	}

	@Override
	public Collection<Firewall> list() throws InternalException, CloudException {

		ArrayList<Firewall> list = new ArrayList<Firewall>();

		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("resourceKey.name");
			condition.getValue().add(FCOProviderUtils.DASEIN_FIREWALL_KEY);
			filter.getFilterConditions().add(condition);
			
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL_TEMPLATE);

			if(result == null || result.getList() == null || result.getList().size() == 0){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createFirewallFromFirewallTemplate((FirewallTemplate) obj, getContext().getRegionId()));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan) throws InternalException, CloudException {
		ArrayList<RuleTargetType> list = new ArrayList<RuleTargetType>();
		if(inVlan){
			return list;
		}		
		
		list.add(RuleTargetType.VM);
		return list;
	}

	@Override
	public Iterable<Direction> listSupportedDirections(boolean inVlan) throws InternalException, CloudException {
		ArrayList<Direction> list = new ArrayList<Direction>();
		if(inVlan){
			return list;
		}	
		
		list.add(Direction.EGRESS);
		list.add(Direction.INGRESS);
		return list;
	}

	@Override
	public Iterable<Permission> listSupportedPermissions(boolean inVlan) throws InternalException, CloudException {
		ArrayList<Permission> list = new ArrayList<Permission>();
		if(inVlan){
			return list;
		}	
		
		list.add(Permission.ALLOW);
		list.add(Permission.DENY);
		return list;
	}
	@Override
	public boolean supportsFirewallSources() throws CloudException, InternalException {
		return false;
	}

	@Override
	public Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan) throws InternalException, CloudException {
		ArrayList<RuleTargetType> list = new ArrayList<RuleTargetType>();
		if(inVlan){
			return list;
		}	
		
		list.add(RuleTargetType.VM);
		return list;
	}

	@Override
	@Nonnull
	public Iterable<ResourceStatus> listFirewallStatus() throws InternalException, CloudException {
		ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
		
		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("resourceKey.name");
			condition.getValue().add(FCOProviderUtils.DASEIN_FIREWALL_KEY);
			filter.getFilterConditions().add(condition);
			
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL_TEMPLATE);
			
			if(result == null || result.getList() == null){
				return list;
			}
			
			for(Object obj : result.getList()){
				FirewallTemplate template = (FirewallTemplate) obj;
				
				list.add(new ResourceStatus(template.getResourceUUID(), true));
			}
			
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		
		return list;
	}

	@Override
	@Nonnull
	public String create(@Nonnull FirewallCreateOptions options) throws InternalException, CloudException {
		
		if(FCOProviderUtils.isSet(options.getProviderVlanId())){
			throw new OperationNotSupportedException("VLAN firewall creation is not supported");
		}
		
		FirewallTemplate firewallTemplate = new FirewallTemplate();

		if(options != null){

			ResourceMetadata metadata = new ResourceMetadata();
			metadata.setPublicMetadata(FCOProviderUtils.convertMapToXML(options.getMetaData()));
			firewallTemplate.setResourceMetadata(metadata);
			firewallTemplate.setResourceName(options.getName());
			
		}
		
		firewallTemplate.setType(IpType.IPV_4);
		firewallTemplate.setDefaultInAction(FirewallRuleAction.ALLOW);
		firewallTemplate.setDefaultOutAction(FirewallRuleAction.ALLOW);
		firewallTemplate.setClusterUUID(getContext().getRegionId());
		
		ResourceKey daseinKey = new ResourceKey();
		daseinKey.setName(FCOProviderUtils.DASEIN_FIREWALL_KEY);
		daseinKey.setType(ResourceKeyType.USER_KEY);
		daseinKey.setValue("Not attached to server");
		daseinKey.setWeight(0.0);
		
		firewallTemplate.getResourceKey().add(daseinKey);

		try{
			Job job = userService.createFirewallTemplate(firewallTemplate, null);
			userService.waitForJob(job.getResourceUUID(), true);

			return job.getItemUUID();
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nullable
	public Firewall getFirewall(@Nonnull String firewallId) throws InternalException, CloudException {
		try{
			return createFirewallFromFirewallTemplate((FirewallTemplate) FCOProviderUtils.getResource(userService, firewallId, ResourceType.FIREWALL_TEMPLATE), getContext().getRegionId());
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Collection<FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
		return new ArrayList<FirewallRule>();
	}

	// ---------- Helper Methods ---------- //

	private static Firewall createFirewallFromFirewallTemplate(FirewallTemplate firewallTemplate, String regionID) {

		if(firewallTemplate == null){
			return null;
		}

		Firewall firewall = new Firewall();

		firewall.setActive(firewallTemplate.getResourceState() == ResourceState.ACTIVE);
		firewall.setAvailable(firewallTemplate.getResourceState() == ResourceState.ACTIVE);
		firewall.setDescription(firewallTemplate.getResourceName());
		firewall.setName(firewallTemplate.getResourceName());
		firewall.setProviderFirewallId(firewallTemplate.getResourceUUID());
		firewall.setRegionId(FCOProviderUtils.isSet(firewallTemplate.getClusterUUID()) ? firewallTemplate.getClusterUUID() : regionID);
		firewall.setProviderVlanId(firewallTemplate.getServiceNetworkUUID());

		return firewall;
	}
}
