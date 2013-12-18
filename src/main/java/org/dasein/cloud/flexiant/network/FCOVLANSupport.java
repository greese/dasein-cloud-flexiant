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

import javax.annotation.Nonnull;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.network.AbstractVLANSupport;
import org.dasein.cloud.network.IPVersion;
import org.dasein.cloud.network.NICState;
import org.dasein.cloud.network.NetworkInterface;
import org.dasein.cloud.network.RawAddress;
import org.dasein.cloud.network.Subnet;
import org.dasein.cloud.network.SubnetCreateOptions;
import org.dasein.cloud.network.SubnetState;
import org.dasein.cloud.network.VLAN;
import org.dasein.cloud.network.VLANState;
import org.dasein.cloud.network.VlanCreateOptions;

import com.extl.jade.user.Condition;
import com.extl.jade.user.Customer;
import com.extl.jade.user.Customer.LimitsMap;
import com.extl.jade.user.Customer.LimitsMap.Entry;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Ip;
import com.extl.jade.user.Job;
import com.extl.jade.user.Limits;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Network;
import com.extl.jade.user.NetworkType;
import com.extl.jade.user.Nic;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.Server;
import com.extl.jade.user.UserService;

/**
 * The AbstractVLANSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOVLANSupport extends AbstractVLANSupport {

	private UserService userService = null;

	public FCOVLANSupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public boolean allowsNewNetworkInterfaceCreation() throws CloudException, InternalException {
		return false;
	}

	@Override
	public boolean allowsNewSubnetCreation() throws CloudException, InternalException {
		return false;
	}

	@Override
	public boolean allowsNewVlanCreation() throws CloudException, InternalException {
		return true;
	}

	@Override
	@Nonnull
	public Subnet createSubnet(@Nonnull SubnetCreateOptions options) throws CloudException, InternalException {
		throw new OperationNotSupportedException("Creating a new Subnet is not suppored in " + getProvider().getProviderName());
//		if(options == null){
//			throw new InternalException("Cannot create Subnet as SubnetCreateOptions is null");
//		}
//
//		com.extl.jade.user.Subnet jadeSubnet = new com.extl.jade.user.Subnet();
//
//		jadeSubnet.setDefaultGatewayAddress(options.getCidr());
//		jadeSubnet.setResourceName(options.getName());
//		jadeSubnet.setVdcUUID(options.getProviderDataCenterId());
//		jadeSubnet.setNetworkUUID(options.getProviderVlanId());
//
//		for(IPVersion ipVersion : options.getSupportedTraffic()){
//			if(ipVersion == IPVersion.IPV4 && jadeSubnet.getSubnetType() != IpType.IPV_6){
//				jadeSubnet.setSubnetType(IpType.IPV_4);
//			}else if(ipVersion == IPVersion.IPV6){
//				jadeSubnet.setSubnetType(IpType.IPV_6);
//			}
//		}
//
//		try{
//			Job job = userService.createSubnet(jadeSubnet, null);
//			userService.waitForJob(job.getResourceUUID(), true);
//
//			return createSubnetFromSubnet((com.extl.jade.user.Subnet) FCOProviderUtils.getResource(userService, job.getItemUUID(), ResourceType.SUBNET, true));
//
//		}catch (ExtilityException e){
//			throw new CloudException(e);
//		}
	}

	@Override
	@Nonnull
	public VLAN createVlan(@Nonnull String cidr, @Nonnull String name, @Nonnull String description, @Nonnull String domainName, @Nonnull String[] dnsServers, @Nonnull String[] ntpServers) throws CloudException, InternalException {
		return createVlan(VlanCreateOptions.getInstance(name, description, cidr, domainName, dnsServers, ntpServers));
	}

	@Override
	@Nonnull
	public VLAN createVlan(@Nonnull VlanCreateOptions options) throws CloudException, InternalException {

		Network network = new Network();

		network.setResourceName(options.getName());
		network.setNetworkType(NetworkType.PUBLIC);
		network.setClusterUUID(getProvider().getContext().getRegionId());
		network.setVdcUUID(FCOProviderUtils.getDefaultVDC(userService, network.getClusterUUID(), getProvider().getContext().getAccountNumber()));

		try{
			Job job = userService.createNetwork(network, null);
			userService.waitForJob(job.getResourceUUID(), true);

			return createVLANFromNetwork(userService, (Network) FCOProviderUtils.getResource(userService, job.getItemUUID(), ResourceType.NETWORK, true), options.getCidr());
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public int getMaxVlanCount() throws CloudException, InternalException {
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("resourceUUID");
		fc.getValue().add(getProvider().getContext().getAccountNumber());
		filter.getFilterConditions().add(fc);
		try{
			Customer customer = (Customer) FCOProviderUtils.getResource(userService, getProvider().getContext().getAccountNumber(), ResourceType.CUSTOMER, false);
			if(customer == null){
				return -1;
			}

			LimitsMap limits = customer.getLimitsMap();
			if(limits != null){
				for(Entry entry : limits.getEntry()){
					if(entry.getKey() == Limits.MAX_NETWORK_PUBLIC)
						return entry.getValue();
				}

			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return -1;
	}

	@Override
	public NetworkInterface getNetworkInterface(@Nonnull String nicId) throws CloudException, InternalException {
		try{
			return createNetworkInterfaceFromNic((Nic) FCOProviderUtils.getResource(userService, nicId, ResourceType.NIC, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public String getProviderTermForNetworkInterface(Locale locale) {
		return "NIC";
	}

	@Override
	public String getProviderTermForSubnet(Locale locale) {
		return "Subnet";
	}

	@Override
	public String getProviderTermForVlan(Locale locale) {
		return "Network";
	}

	@Override
	public Subnet getSubnet(String providerSubnetId) throws CloudException, InternalException {
		try{
			return createSubnetFromSubnet((com.extl.jade.user.Subnet) FCOProviderUtils.getResource(userService, providerSubnetId, ResourceType.SUBNET, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public Requirement getSubnetSupport() throws CloudException, InternalException {
		return Requirement.OPTIONAL;
	}

	@Override
	public VLAN getVlan(String providerVlanId) throws CloudException, InternalException {
		if(!FCOProviderUtils.isSet(providerVlanId)){
			throw new InternalException("VLAN id was not valid");
		}

		try{
			return createVLANFromNetwork(userService, (Network) FCOProviderUtils.getResource(userService, providerVlanId, ResourceType.NETWORK, true), null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public Requirement identifySubnetDCRequirement() {
		return Requirement.OPTIONAL;
	}

	@Override
	public boolean isNetworkInterfaceSupportEnabled() throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean isSubnetDataCenterConstrained() throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean isVlanDataCenterConstrained() throws CloudException, InternalException {
		return true;
	}

	@Override
	@Nonnull
	public Iterable<NetworkInterface> listNetworkInterfaces() throws CloudException, InternalException {
		List<NetworkInterface> list = new ArrayList<NetworkInterface>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.NIC);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createNetworkInterfaceFromNic((Nic) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<Subnet> listSubnets(String inVlanId) throws CloudException, InternalException {
		List<Subnet> subnets = new ArrayList<Subnet>();
		if(!FCOProviderUtils.isSet(inVlanId)){
			return subnets;
		}

		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("networkUUID");
			condition.getValue().add(inVlanId);
			filter.getFilterConditions().add(condition);

			ListResult result = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.SUBNET);
			if(result == null || result.getList() == null){
				return subnets;
			}

			for(Object obj : result.getList()){
				subnets.add(createSubnetFromSubnet((com.extl.jade.user.Subnet) obj));
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return subnets;
	}

	@Override
	public Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
		List<IPVersion> versions = new ArrayList<IPVersion>();
		versions.add(IPVersion.IPV4);
		versions.add(IPVersion.IPV6);
		return versions;
	}

	@Override
	public Iterable<VLAN> listVlans() throws CloudException, InternalException {
		List<VLAN> vlans = new ArrayList<VLAN>();
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("networkType");
		fc.getValue().add(NetworkType.PUBLIC.name());
		filter.getFilterConditions().add(fc);
		try{
			ListResult result = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.NETWORK);
			if(result == null || result.getList() == null){
				return vlans;
			}
			for(Object obj : result.getList()){
				vlans.add(createVLANFromNetwork(userService, (Network) obj, null));
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return vlans;
	}

	@Override
	public Iterable<ResourceStatus> listVlanStatus() throws CloudException, InternalException {
		List<ResourceStatus> vlanStatus = new ArrayList<ResourceStatus>();
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("networkType");
		condition.getValue().add(NetworkType.PUBLIC.name());
		filter.getFilterConditions().add(condition);

		try{

			ListResult result = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.NETWORK);
			if(result == null || result.getList() == null){
				return null;
			}

			for(Object obj : result.getList()){
				Network network = (Network) obj;
				vlanStatus.add(new ResourceStatus(network.getResourceUUID(), getVlanStateFromResourceState(network.getResourceState())));
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return vlanStatus;
	}

	@Override
	public void removeSubnet(String providerSubnetId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("Deleting Subnet is not supported in " + getProvider().getProviderName());
//		try{
//			Job job = userService.deleteResource(providerSubnetId, true, null);
//			userService.waitForJob(job.getResourceUUID(), true);
//		}catch (ExtilityException e){
//			throw new CloudException(e);
//		}
	}

	@Override
	public void removeVlan(String vlanId) throws CloudException, InternalException {
		try{
			Job job = userService.deleteResource(vlanId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	// ---------- Helper Methods ---------- //

	private static NetworkInterface createNetworkInterfaceFromNic(Nic nic) {
		NetworkInterface networkInterface = new NetworkInterface();

		networkInterface.setCurrentState(getNICStateFromResourceState(nic.getResourceState()));
		networkInterface.setDescription(nic.getResourceName());

		// networkInterface.setDnsName(dnsName);

		List<RawAddress> rawAddresses = new ArrayList<RawAddress>();
		for(Ip ipAddress : nic.getIpAddresses()){
			rawAddresses.add(new RawAddress(ipAddress.getIpAddress()));
		}

		networkInterface.setIpAddresses(rawAddresses.toArray(new RawAddress[0]));
		networkInterface.setMacAddress(nic.getMacAddress());
		networkInterface.setName(nic.getResourceName());
		networkInterface.setProviderDataCenterId(nic.getVdcUUID());
		networkInterface.setProviderNetworkInterfaceId(nic.getResourceUUID());
		networkInterface.setProviderOwnerId(nic.getCustomerUUID());
		networkInterface.setProviderRegionId(nic.getClusterUUID());

		// networkInterface.setProviderSubnetId(providerSubnetId);

		networkInterface.setProviderVirtualMachineId(nic.getServerUUID());
		networkInterface.setProviderVlanId(nic.getNetworkUUID());

		return networkInterface;
	}

	private static Subnet createSubnetFromSubnet(com.extl.jade.user.Subnet fcoSubnet) {
		if(fcoSubnet == null){
			return null;
		}

		SubnetState state = null;
		switch(fcoSubnet.getResourceState()){
		case CREATING:
		case TO_BE_DELETED:
		case DELETED:
		case LOCKED:
			state = SubnetState.PENDING;
			break;
		case HIDDEN:
		case ACTIVE:
			state = SubnetState.AVAILABLE;
			break;
		default:
			break;
		}
		Subnet subnet = Subnet.getInstance(fcoSubnet.getCustomerUUID(), fcoSubnet.getClusterUUID(), fcoSubnet.getNetworkUUID(), fcoSubnet.getResourceUUID(), state, fcoSubnet.getResourceName(), "Subnet : " + fcoSubnet.getResourceName(), fcoSubnet.getDefaultGatewayAddress());

		subnet.constrainedToDataCenter(fcoSubnet.getVdcUUID());

		return subnet;
	}

	private static VLAN createVLANFromNetwork(UserService userService, Network network, String cidrFromCreate) {
		if(network == null){
			return null;
		}

		VLAN vlan = new VLAN();
		vlan.setName(network.getResourceName());
		vlan.setNetworkType(network.getNetworkType().name());
		vlan.setProviderDataCenterId(network.getVdcUUID());
		vlan.setProviderOwnerId(network.getCustomerUUID());
		vlan.setProviderRegionId(network.getClusterUUID());
		vlan.setProviderVlanId(network.getResourceUUID());
		vlan.setSupportedTraffic(IPVersion.IPV4, IPVersion.IPV6);
		if(FCOProviderUtils.isSet(cidrFromCreate)){
			vlan.setCidr(cidrFromCreate);
		}else if(network.getSubnets().size() > 0){
			com.extl.jade.user.Subnet subnet = network.getSubnets().get(0);
			vlan.setCidr(subnet.getDefaultGatewayAddress());
		}else{
			// If the network has no subnets we still need to specify a CIDR
			vlan.setCidr("28", "127.0.0.1"); // TODO : if network does not have subnet, should we throw exception?
		}

		vlan.setCurrentState(getVlanStateFromResourceState(network.getResourceState()));
		List<String> servers = listServerIds(userService, network.getResourceUUID());
		String[] array = new String[servers.size()];
		vlan.setDnsServers(listServerIds(userService, network.getResourceUUID()).toArray(array));
		vlan.setDescription("Network : " + network.getResourceName());

		return vlan;
	}

	private static NICState getNICStateFromResourceState(ResourceState resourceState) {
		switch(resourceState){
		case ACTIVE:
			return NICState.IN_USE;
		case CREATING:
			return NICState.PENDING;
		case DELETED:
		case HIDDEN:
		case LOCKED:
		case TO_BE_DELETED:
		default:
			return NICState.DELETED;

		}
	}

	private static VLANState getVlanStateFromResourceState(ResourceState status) {
		switch(status){
		case CREATING:
		case TO_BE_DELETED:
		case DELETED:
		case LOCKED:
			return VLANState.PENDING;
		case HIDDEN:
		case ACTIVE:
		default:
			return VLANState.AVAILABLE;
		}
	}

	private static List<String> listServerIds(UserService userService, String networkUUID) {
		List<String> serversList = new ArrayList<String>();
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("nics.networkUUID");
		fc.getValue().add(networkUUID);
		filter.getFilterConditions().add(fc);
		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.SERVER);
			if(result == null || result.getList() == null){
				return serversList;
			}
			for(Object object : result.getList()){
				Server server = (Server) object;
				serversList.add(server.getResourceUUID());
			}
		}catch (ExtilityException e){

		}
		return serversList;
	}

}
