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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ci.AbstractConveredInfrastructureSupport;
import org.dasein.cloud.ci.CIFilterOptions;
import org.dasein.cloud.ci.CIProvisionOptions;
import org.dasein.cloud.ci.ConvergedInfrastructure;
import org.dasein.cloud.ci.ConvergedInfrastructureState;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;

import com.extl.jade.user.Condition;
import com.extl.jade.user.DeploymentInstance;
import com.extl.jade.user.DeploymentInstanceStatus;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Network;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.Server;
import com.extl.jade.user.UserService;

/**
 * The AbstractConveredInfrastructureSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOConvergedInfrastructureSupport extends AbstractConveredInfrastructureSupport<FCOProvider> {

	private UserService userService;

	public FCOConvergedInfrastructureSupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Iterable<ConvergedInfrastructure> listConvergedInfrastructures(CIFilterOptions options) throws CloudException, InternalException {
		List<ConvergedInfrastructure> ciList = new ArrayList<ConvergedInfrastructure>();
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_GREATER_THAN);
		condition.setField("resourceState");
		condition.getValue().add(ResourceState.ACTIVE.name());
		filter.getFilterConditions().add(condition);
		try{
			List<Object> diList = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DEPLOYMENT_INSTANCE).getList();
			if(diList.size() > 0){
				DeploymentInstance instance = (DeploymentInstance) diList.get(0);
				ConvergedInfrastructureState state = null;
				switch(instance.getStatus()){
				case ERROR:
				case STARTING:
				case STOPPING:
				case STOPPED:
				case REBOOTING:
				case INSTALLING:
				case MIGRATING:
				case RECOVERY:
				case BUILDING:
					state = ConvergedInfrastructureState.PENDING;
					break;
				case RUNNING:
					state = ConvergedInfrastructureState.RUNNING;
					break;
				case DELETING:
					state = ConvergedInfrastructureState.DELETED;
					break;
				default:
					break;
				}
				ConvergedInfrastructure ci = ConvergedInfrastructure.getInstance(instance.getCustomerUUID(), instance.getClusterUUID(), instance.getResourceUUID(), state, instance.getResourceName(), null);
				ciList.add(ci);
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return ciList;
	}

	@Override
	public Iterable<String> listVirtualMachines(String inCIId) throws InternalException, CloudException {
		List<String> serverUUIDs = new ArrayList<String>();
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("deploymentInstanceUUID");
		condition.getValue().add(inCIId);
		filter.getFilterConditions().add(condition);
		condition = new FilterCondition();
		condition.setCondition(Condition.IS_GREATER_THAN);
		condition.setField("resourceState");
		condition.getValue().add(ResourceState.ACTIVE.name());
		filter.getFilterConditions().add(condition);
		try{
			List<Object> diList = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DEPLOYMENT_INSTANCE).getList();
			if(diList.size() > 0){
				DeploymentInstance instance = (DeploymentInstance) diList.get(0);
				List<Server> servers = instance.getServer();
				for(Server server : servers){
					serverUUIDs.add(server.getResourceUUID());
				}
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return serverUUIDs;
	}

	@Override
	public Iterable<String> listVLANs(String inCIId) throws CloudException, InternalException {
		List<String> vlanUUIDs = new ArrayList<String>();
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("deploymentInstanceUUID");
		condition.getValue().add(inCIId);
		filter.getFilterConditions().add(condition);
		condition = new FilterCondition();
		condition.setCondition(Condition.IS_GREATER_THAN);
		condition.setField("resourceState");
		condition.getValue().add(ResourceState.ACTIVE.name());
		filter.getFilterConditions().add(condition);
		try{
			List<Object> diList = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DEPLOYMENT_INSTANCE).getList();
			if(diList.size() > 0){
				DeploymentInstance instance = (DeploymentInstance) diList.get(0);
				List<Network> networks = instance.getNetwork();
				for(Network network : networks){
					vlanUUIDs.add(network.getResourceUUID());
				}
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return vlanUUIDs;
	}

	@Override
	public ConvergedInfrastructure provision(CIProvisionOptions options) throws CloudException, InternalException {
		if(options != null && options.getTopologyId() != null){
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("deploymentTemplateUUID");
			condition.getValue().add(options.getTopologyId());
			filter.getFilterConditions().add(condition);
			try{
				List<Object> diList = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DEPLOYMENT_INSTANCE).getList();
				if(diList.size() > 0){
					DeploymentInstance instance = (DeploymentInstance) diList.get(0);
					ConvergedInfrastructureState state = null;
					switch(instance.getStatus()){
					case ERROR:
					case STARTING:
					case STOPPING:
					case STOPPED:
					case REBOOTING:
					case INSTALLING:
					case MIGRATING:
					case RECOVERY:
					case BUILDING:
						state = ConvergedInfrastructureState.PENDING;
						break;
					case RUNNING:
						state = ConvergedInfrastructureState.RUNNING;
						break;
					case DELETING:
						state = ConvergedInfrastructureState.DELETED;
						break;
					default:
						break;
					}
					ConvergedInfrastructure ci = ConvergedInfrastructure.getInstance(instance.getClusterUUID(), instance.getClusterUUID(), instance.getResourceUUID(), state, instance.getResourceName(), null);
					return ci;
				}
			}catch (ExtilityException e){
				throw new CloudException(e);
			}
		}
		return null;
	}

	@Override
	public void terminate(String ciId, String explanation) throws CloudException, InternalException {
		try{
			userService.changeDeploymentInstanceStatus(ciId, DeploymentInstanceStatus.STOPPED, true, null, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

}
