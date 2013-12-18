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
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.network.AbstractNetworkFirewallSupport;
import org.dasein.cloud.network.Direction;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.Permission;
import org.dasein.cloud.network.Protocol;
import org.dasein.cloud.network.RuleTarget;

import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.FirewallProtocol;
import com.extl.jade.user.FirewallRuleAction;
import com.extl.jade.user.FirewallTemplate;
import com.extl.jade.user.Ip;
import com.extl.jade.user.Nic;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserService;

/**
 * The AbstractNetworkFirewallSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCONetworkFirewallSupport extends AbstractNetworkFirewallSupport {
		
	private UserService userService = null;
	
	public FCONetworkFirewallSupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public String getProviderTermForNetworkFirewall(Locale locale) {
		return "Firewall";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Collection<Firewall> listFirewalls() throws InternalException, CloudException {
		List<Firewall> firewalls = new ArrayList<Firewall>();		
		try {
			List<Object> fcoFirewalls = FCOProviderUtils.listAllResources(userService, null, ResourceType.FIREWALL).getList();
			if(fcoFirewalls.size() == 0){
				return firewalls;
			}
			for(Object obj : firewalls){
				Firewall firewall = createFirewall((com.extl.jade.user.Firewall)obj);
				firewalls.add(firewall);
			}
		} catch (ExtilityException e) {
			e.printStackTrace();
			throw new CloudException(e.getLocalizedMessage());
		}
		return firewalls;
	}

	@Override
	public Iterable<FirewallRule> listRules(String firewallId) throws InternalException, CloudException {
		List<FirewallRule> firewallRules = new ArrayList<FirewallRule>();		
		try {
			SearchFilter filter = new SearchFilter();
			FilterCondition fc = new FilterCondition();
			fc.setCondition(Condition.IS_EQUAL_TO);
			fc.setField("resourceUUID");
			fc.getValue().add(firewallId);
			filter.getFilterConditions().add(fc);
			
			List<Object> fcoFirewalls = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL).getList();
			if(fcoFirewalls.size() == 0){
				return firewallRules;
			}
			com.extl.jade.user.Firewall fcoFirewall = (com.extl.jade.user.Firewall)fcoFirewalls.get(0);
			
			filter = new SearchFilter();
			fc = new FilterCondition();
			fc.setCondition(Condition.IS_EQUAL_TO);
			fc.setField("resourceUUID");
			fc.getValue().add(fcoFirewall.getTemplateUUID());
			filter.getFilterConditions().add(fc);
			
			List<Object> fcoFirewallTemplates = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL_TEMPLATE).getList();
			if(fcoFirewallTemplates.size() == 0){
				return firewallRules;
			}
			FirewallTemplate fcoFwTemplate = (FirewallTemplate)fcoFirewallTemplates.get(0);
			List<com.extl.jade.user.FirewallRule> fcoFirewallRules = fcoFwTemplate.getFirewallInRuleList();
			for(com.extl.jade.user.FirewallRule rule: fcoFirewallRules){
				firewallRules.add(createFirewallRule(rule, fcoFirewall.getResourceUUID(), "IN", fcoFwTemplate.getServiceNetworkUUID()));
			}
		} catch (ExtilityException e) {
			e.printStackTrace();
			throw new CloudException(e.getLocalizedMessage());
		}
		return firewallRules;
	}

	@Override
	public void removeFirewall(String... firewallIds) throws InternalException, CloudException {
		if(firewallIds != null && firewallIds.length > 0)
			try {
				for(String firewallId : firewallIds)				
					userService.deleteResource(firewallId, true, null);
			} catch (ExtilityException e) {
				e.printStackTrace();
				throw new CloudException(e.getLocalizedMessage());
			}
	}
	
	@Override
	public Firewall getFirewall(String arg0) throws InternalException,
			CloudException {
		Firewall firewall = null;
		if(arg0 == null)
			return firewall;
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("resourceUUID");
		fc.getValue().add(arg0);
		filter.getFilterConditions().add(fc);
		try {
			List<Object> fcoFirewalls = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL).getList();			
			if(fcoFirewalls.size() == 0){
				return firewall;
			}
			firewall = createFirewall((com.extl.jade.user.Firewall)fcoFirewalls.get(0));
		} catch (ExtilityException e) {
			e.printStackTrace();
			throw new CloudException(e.getMessage());
		}
	
		return firewall;
	}
	
	@Override
	public Iterable<ResourceStatus> listFirewallStatus()
			throws InternalException, CloudException {
		List<ResourceStatus> resourceStatesList = new ArrayList<ResourceStatus>();		
		try {
			List<Object> fcoFirewalls = FCOProviderUtils.listAllResources(userService, null, ResourceType.FIREWALL).getList();			
			if(fcoFirewalls.size() == 0){
				return resourceStatesList;
			}
			for(Object obj : fcoFirewalls){
				com.extl.jade.user.Firewall firewall = (com.extl.jade.user.Firewall)obj;
				ResourceStatus status = new ResourceStatus(firewall.getResourceUUID(), null);
				resourceStatesList.add(status);
			}
		} catch (ExtilityException e) {
			e.printStackTrace();
			throw new CloudException(e.getMessage());
		}
		return super.listFirewallStatus();
	}
	
	private Firewall createFirewall(com.extl.jade.user.Firewall fcoFirewall) throws CloudException{
		Firewall firewall = new Firewall();
		firewall.setActive(fcoFirewall.getResourceState() == ResourceState.ACTIVE);
		firewall.setName(fcoFirewall.getResourceName());
		firewall.setProviderFirewallId(fcoFirewall.getResourceUUID());
		firewall.setDescription("Firewall : "+fcoFirewall.getResourceName());
		
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("ipAddresses.ipAddress");
		fc.getValue().add(fcoFirewall.getIpAddress());
		filter.getFilterConditions().add(fc);
		
		try {
			List<Object> nics = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.NIC).getList();
			if(nics.size() > 0){
				Nic nic = (Nic)nics.get(0);
				firewall.setProviderVlanId(nic.getNetworkUUID());
				firewall.setRegionId(nic.getClusterUUID());
				for(Ip ip : nic.getIpAddresses()){
					if(fcoFirewall.getIpAddress().equals(ip.getIpAddress()))
						firewall.setSubnetAssociations(new String[]{ip.getSubnetUUID()});
				}
			}
		} catch (ExtilityException e) {
			e.printStackTrace();
			throw new CloudException(e.getLocalizedMessage());
		}
		return firewall;
	}
	
	@SuppressWarnings("deprecation")
	private FirewallRule createFirewallRule(com.extl.jade.user.FirewallRule fcoFirewallRule,String firewallId,String inOrOut,String vlanId){
		Direction direction = null;
		if("IN".equals(inOrOut))
			direction = Direction.INGRESS;
		else
			direction = Direction.EGRESS;
		FirewallProtocol fcoProtocol = fcoFirewallRule.getProtocol();
		Protocol protocol = null;
		switch (fcoProtocol) {
			case ANY:
				protocol = Protocol.ANY;
				break;
			case ICMP:
				protocol = Protocol.ICMP;
				break;
			case TCP:
				protocol = Protocol.TCP;
				break;
			case UDP:
				protocol = Protocol.UDP;
				break;
			default:
				break;
		}
		
		Permission permission = null;
		if(fcoFirewallRule.getAction() == FirewallRuleAction.ALLOW){
			permission = Permission.ALLOW;
		}else if (fcoFirewallRule.getAction() == FirewallRuleAction.REJECT){
			permission = Permission.DENY;
		}
		RuleTarget target = RuleTarget.getVlan(vlanId);
		String fwRuleId = FirewallRule.getRuleId(firewallId, fcoFirewallRule.getIpAddress(), direction, 
				protocol, permission, target, fcoFirewallRule.getLocalPort(), fcoFirewallRule.getRemotePort());
		FirewallRule rule = FirewallRule.getInstance(fwRuleId, firewallId, fcoFirewallRule.getIpAddress(), direction, protocol, permission, target, fcoFirewallRule.getRemotePort());
		return rule;
	}

}
