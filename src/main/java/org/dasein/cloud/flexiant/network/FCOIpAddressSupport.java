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
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.network.AddressType;
import org.dasein.cloud.network.IPVersion;
import org.dasein.cloud.network.IpAddress;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.IpForwardingRule;
import org.dasein.cloud.network.Protocol;

import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Ip;
import com.extl.jade.user.IpType;
import com.extl.jade.user.Job;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.NetworkType;
import com.extl.jade.user.Nic;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.Subnet;
import com.extl.jade.user.UserService;

/**
 * The IpAddressSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOIpAddressSupport implements IpAddressSupport {

	private UserService userService = null;

	public FCOIpAddressSupport(FCOProvider provider) {
		super();
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public String[] mapServiceAction(ServiceAction action) {
		return new String[0];
	}

	@Override
	public void assign(String addressId, String serverId) throws InternalException, CloudException {
		

	}

	@Override
	public void assignToNetworkInterface(String addressId, String nicId) throws InternalException, CloudException {
		try{
			Job job = userService.addIP(nicId, addressId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public String forward(String addressId, int publicPort, Protocol protocol, int privatePort, String onServerId) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public IpAddress getIpAddress(String addressId) throws InternalException, CloudException {
		if(!FCOProviderUtils.isSet(addressId)){
			return null;
		}

		IpAddress ipAddress = new IpAddress();
		ipAddress.setAddress(addressId);
		ipAddress.setIpAddressId(addressId);
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("ipAddresses.ipAddress");
		fc.getValue().add(addressId);
		filter.getFilterConditions().add(fc);
		try{
			List<Object> nics = FCOProviderUtils.listAllResources(userService, filter, ResourceType.NIC).getList();
			if(nics.size() == 0){
				return null;
			}
			completeIPAddressFromNic((Nic) nics.get(0), ipAddress);
		}catch (ExtilityException e){
			e.printStackTrace();
			throw new CloudException(e.getLocalizedMessage());
		}
		return ipAddress;
	}

	@Override
	public String getProviderTermForIpAddress(Locale locale) {
		return "IP";
	}

	@Override
	public Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException {
		return Requirement.REQUIRED;
	}

	@Override
	public boolean isAssigned(AddressType type) {
		if(type == AddressType.PUBLIC){
			return true;
		}
		return false;
	}

	@Override
	public boolean isAssigned(IPVersion version) throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean isAssignablePostLaunch(IPVersion version) throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public boolean isForwarding() {
		return false;
	}

	@Override
	public boolean isForwarding(IPVersion version) throws CloudException, InternalException {
		if(version == IPVersion.IPV4)
			return true;
		else
			return false; 
	}

	@Override
	public boolean isRequestable(AddressType type) {
		return type == AddressType.PUBLIC;
	}

	@Override
	public boolean isRequestable(IPVersion version) throws CloudException, InternalException {
		if(version == IPVersion.IPV4)
			return true;
		else
			return false;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return false;
	}

	@Override
	public Iterable<IpAddress> listPrivateIpPool(boolean unassignedOnly) throws InternalException, CloudException {
		return new ArrayList<IpAddress>();
	}

	@Override
	public Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly) throws InternalException, CloudException {
		return listIpPool(null, unassignedOnly);
	}

	@Override
	public Iterable<IpAddress> listIpPool(IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
		List<IpAddress> ipAddresslist = new ArrayList<IpAddress>();
		if(version == null){
			return ipAddresslist;
		}

		SearchFilter filter = new SearchFilter();
		if(version != null){
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("subnetType");
			condition.getValue().add(version.name());
			filter.getFilterConditions().add(condition);
		}

		if(unassignedOnly){
			try{
				ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.SUBNET);
				if(result == null || result.getList() == null){
					return ipAddresslist;
				}

				for(Object obj : result.getList()){
					Subnet subnet = (Subnet) obj;
					List<String> useableIps = subnet.getUseableIps();
					for(String ip : useableIps){
						IpAddress address = new IpAddress();
						address.setAddress(ip);
						address.setIpAddressId(ip);
						completeIPAddressFromSubnet(subnet, address);
						ipAddresslist.add(address);
					}
				}

			}catch (ExtilityException e){
				throw new CloudException(e);
			}
		}else{
			try{
				filter = new SearchFilter();
				FilterCondition condition = new FilterCondition();
				condition.setCondition(Condition.IS_EQUAL_TO);
				condition.setField("ipAddresses.type");
				condition.getValue().add(version.name());
				filter.getFilterConditions().add(condition);

				ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.NIC);
				if(result == null || result.getList() == null){
					return ipAddresslist;
				}

				for(Object obj : result.getList()){
					Nic nic = (Nic) obj;
					if(nic.getIpAddresses().size() > 0){
						for(Ip ip : nic.getIpAddresses()){
							IpAddress address = new IpAddress();
							address.setAddress(ip.getIpAddress());
							completeIPAddressFromNic(nic, address);
							ipAddresslist.add(address);
						}
					}
				}

			}catch (ExtilityException e){
				throw new CloudException(e);
			}
		}
		return ipAddresslist;
	}

	@Override
	public Iterable<ResourceStatus> listIpPoolStatus(IPVersion version) throws InternalException, CloudException {
		List<ResourceStatus> resourceStatusList = new ArrayList<ResourceStatus>();
		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("ipAddresses.type");
			condition.getValue().add(version.name());
			filter.getFilterConditions().add(condition);

			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.NIC);
			if(result == null || result.getList() == null){
				return resourceStatusList;
			}

			for(Object obj : result.getList()){
				Nic nic = (Nic) obj;
				if(nic.getIpAddresses().size() > 0){
					for(Ip ip : nic.getIpAddresses()){
						ResourceStatus status = new ResourceStatus(ip.getIpAddress(), null);
						resourceStatusList.add(status);
					}
				}
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return resourceStatusList;
	}

	@Override
	public Iterable<IpForwardingRule> listRules(String addressId) throws InternalException, CloudException {
		List<IpForwardingRule> ipForwardRuleList = new ArrayList<IpForwardingRule>();
		if(!FCOProviderUtils.isSet(addressId)){
			return ipForwardRuleList;
		}

		IpAddress ipAddress = new IpAddress();
		ipAddress.setAddress(addressId);
		ipAddress.setIpAddressId(addressId);
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("ipAddresses.ipAddress");
		condition.getValue().add(addressId);
		filter.getFilterConditions().add(condition);

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.NIC);
			if(result == null || result.getList() == null){
				return ipForwardRuleList;
			}

			for(Object obj : result.getList()){
				Nic nic = (Nic) obj;
				IpForwardingRule rule = new IpForwardingRule();
				rule.setAddressId(addressId);
				rule.setServerId(nic.getServerUUID());
				ipForwardRuleList.add(rule);
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return ipForwardRuleList;

	}

	@Override
	public Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
		List<IPVersion> versions = new ArrayList<IPVersion>();
		versions.add(IPVersion.IPV4);
		//versions.add(IPVersion.IPV6);
		return versions;
	}

	@Override
	public void releaseFromPool(String addressId) throws InternalException, CloudException {
		

	}

	@Override
	public void releaseFromServer(String addressId) throws InternalException, CloudException {
		

	}

	@Override
	public String request(AddressType typeOfAddress) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public String request(IPVersion version) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public String requestForVLAN(IPVersion version) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public String requestForVLAN(IPVersion version, String vlanId) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public void stopForward(String ruleId) throws InternalException, CloudException {
		

	}

	@Override
	public boolean supportsVLANAddresses(IPVersion ofVersion) throws InternalException, CloudException {
		
		return false;
	}

	// ---------- Helper Methods ---------- //

	private static void completeIPAddressFromNic(Nic nic, IpAddress ipAddress) {
		ipAddress.setProviderNetworkInterfaceId(nic.getResourceUUID());
		ipAddress.setProviderVlanId(nic.getNetworkUUID());
		ipAddress.setRegionId(nic.getClusterUUID());
		ipAddress.setServerId(nic.getServerUUID());
		switch(nic.getNetworkType()){
		case PUBLIC:
			ipAddress.setAddressType(AddressType.PUBLIC);
			break;
		case PRIVATE:
			ipAddress.setAddressType(AddressType.PRIVATE);
			break;
		default:
			break;
		}
		IpType ipType = null;
		for(Ip ip : nic.getIpAddresses()){
			if(ip.getIpAddress().equals(ipAddress.getRawAddress())){
				ipType = ip.getType();
				break;
			}

		}
		if(ipType != null){
			IPVersion version = null;
			switch(ipType){
			case IPV_4:
				version = IPVersion.IPV4;
				break;
			case IPV_6:
				ipAddress.setAddressType(AddressType.PRIVATE);
				version = IPVersion.IPV6;
				break;
			default:
				break;
			}
			ipAddress.setVersion(version);
		}
	}

	private static void completeIPAddressFromSubnet(Subnet subnet, IpAddress ipAddress) {
		ipAddress.setVersion(subnet.getSubnetType() == IpType.IPV_4 ? IPVersion.IPV4 : IPVersion.IPV6);
		ipAddress.setProviderVlanId(subnet.getNetworkUUID());
		ipAddress.setRegionId(subnet.getClusterUUID());
		ipAddress.setAddressType(subnet.getNetworkType() == NetworkType.PUBLIC ? AddressType.PUBLIC : AddressType.PRIVATE);
	}

}
