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

package org.dasein.cloud.flexiant.ci;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.ci.AbstractTopologySupport;
import org.dasein.cloud.ci.Topology;
import org.dasein.cloud.ci.Topology.VLANDevice;
import org.dasein.cloud.ci.Topology.VMDevice;
import org.dasein.cloud.ci.TopologyFilterOptions;
import org.dasein.cloud.ci.TopologyState;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.util.uom.storage.Storage;

import com.extl.jade.user.DeploymentTemplate;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Network;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.Server;
import com.extl.jade.user.UserService;

/**
 * The AbstractTopologySupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOTopologySupport extends AbstractTopologySupport<FCOProvider> {

	private UserService userService;

	public FCOTopologySupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public String getProviderTermForTopology(Locale locale) {
		return "DeploymentTemplate";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Iterable<Topology> listTopologies(TopologyFilterOptions options) throws CloudException, InternalException {
		List<Topology> list = new ArrayList<Topology>();
		SearchFilter filter = new SearchFilter();
		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DEPLOYMENT_TEMPLATE);
			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				DeploymentTemplate template = (DeploymentTemplate) obj;

				list.add(createTopologyFromDeploymentTemplate(template, getContext().getRegionId()));
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return list;
	}

	@Override
	@Nullable
	public Topology getTopology(@Nonnull String topologyId) throws CloudException, InternalException {
		try{
			return createTopologyFromDeploymentTemplate((DeploymentTemplate) FCOProviderUtils.getResource(userService, topologyId, ResourceType.DEPLOYMENT_TEMPLATE, true), getContext().getRegionId());
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Iterable<ResourceStatus> listTopologyStatus() throws InternalException, CloudException {
		ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();

		try{
			ListResult result = FCOProviderUtils.listWithChildren(userService, null, ResourceType.DEPLOYMENT_TEMPLATE);
			
			if(result == null || result.getList() == null){
				return list;
			}
			
			for(Object obj : result.getList()){
				
				DeploymentTemplate deploymentTemplate = (DeploymentTemplate) obj;
				
				list.add(new ResourceStatus(deploymentTemplate.getResourceUUID(), getTopologyStateFromResourceState(deploymentTemplate.getResourceState())));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	@Nonnull
	public String[] mapServiceAction(@Nonnull ServiceAction action) {
		return new String[0];
	}

	@Override
	@Nonnull
	public Iterable<Topology> searchPublicTopologies(@Nullable TopologyFilterOptions options) throws CloudException, InternalException {

		ArrayList<Topology> list = new ArrayList<Topology>();

		try{
			ListResult result = FCOProviderUtils.listWithChildren(userService, null, ResourceType.DEPLOYMENT_TEMPLATE);
			
			if(result == null || result.getList() == null){
				return list;
			}
			
			for(Object obj : result.getList()){
				
				Topology topology = createTopologyFromDeploymentTemplate((DeploymentTemplate) obj, getContext().getRegionId());
				
				if(options.matches(topology)){
					list.add(topology);
				}
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public boolean supportsPublicLibrary() throws CloudException, InternalException {
		return true;
	}

	// ----------- Helper Methods --------- //

	private static Topology createTopologyFromDeploymentTemplate(DeploymentTemplate deploymentTemplate, String currentRegion) {
		
		if(deploymentTemplate == null){
			return null;
		}
		
		String regionId = deploymentTemplate.getClusterUUID();
		if(!FCOProviderUtils.isSet(regionId)){
			regionId = currentRegion;
		}
		
		Topology topology = Topology.getInstance(deploymentTemplate.getCustomerUUID(), regionId, deploymentTemplate.getResourceUUID(), getTopologyStateFromResourceState(deploymentTemplate.getResourceState()), deploymentTemplate.getResourceName(), deploymentTemplate.getResourceName());
		if(deploymentTemplate.getResourceCreateDate() != null){
			topology.createdAt(deploymentTemplate.getResourceCreateDate().toGregorianCalendar().getTimeInMillis());
		}
		
		for(Server server : deploymentTemplate.getServer()){
			topology.withVirtualMachines(createVMDeviceFromServer(server));
		}
		for(Network network : deploymentTemplate.getNetwork()){
			topology.withVLANs(createVLANDeviceFromNetwork(network));
		}

		return topology;
	}

	private static TopologyState getTopologyStateFromResourceState(ResourceState resourceState) {
		switch(resourceState){
		case ACTIVE:
			return TopologyState.ACTIVE;
		case DELETED:
		case TO_BE_DELETED:
			return TopologyState.DELETED;
		case CREATING:
			return TopologyState.PENDING;
		case HIDDEN:
		case LOCKED:
		default:
			return TopologyState.OFFLINE;
		}
	}
	
	private static VMDevice createVMDeviceFromServer(Server server){
		return VMDevice.getInstance(server.getResourceUUID(), server.getDisks().size(), server.getResourceName(), server.getCpu(), Storage.valueOf(server.getRam(),"mb"), Architecture.I32, Platform.guess(server.getImageName()));
	}
	private static VLANDevice createVLANDeviceFromNetwork(Network network){
		return VLANDevice.getInstance(network.getResourceUUID(), network.getResourceName());
	}
}
