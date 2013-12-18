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

package org.dasein.cloud.flexiant.compute;

import java.util.Collection;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.compute.AutoScalingSupport;
import org.dasein.cloud.compute.LaunchConfiguration;
import org.dasein.cloud.compute.ScalingGroup;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;

/**
 * The AutoScalingSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOAutoScalingSupport implements AutoScalingSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;

	public FCOAutoScalingSupport(FCOProvider provider) {
		this.provider = provider;
	}

	@Override
	public String[] mapServiceAction(ServiceAction action) {
		return null;
	}

	@Override
	public String createAutoScalingGroup(String name, String launchConfigurationId, int minServers, int maxServers, int cooldown, String... dataCenterIds) throws InternalException, CloudException {
		return null;
	}

	@Override
	public String createLaunchConfiguration(String name, String imageId, VirtualMachineProduct size, String... firewalls) throws InternalException, CloudException {
		return null;
	}

	@Override
	public void deleteAutoScalingGroup(String providerAutoScalingGroupId) throws CloudException, InternalException {

	}

	@Override
	public void deleteLaunchConfiguration(String providerLaunchConfigurationId) throws CloudException, InternalException {

	}

	@Override
	public LaunchConfiguration getLaunchConfiguration(String providerLaunchConfigurationId) throws CloudException, InternalException {
		return null;
	}

	@Override
	public ScalingGroup getScalingGroup(String providerScalingGroupId) throws CloudException, InternalException {
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return false;
	}

	@Override
	public Iterable<ResourceStatus> listScalingGroupStatus() throws CloudException, InternalException {
		return null;
	}

	@Override
	public Collection<ScalingGroup> listScalingGroups() throws CloudException, InternalException {
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listLaunchConfigurationStatus() throws CloudException, InternalException {
		return null;
	}

	@Override
	public Collection<LaunchConfiguration> listLaunchConfigurations() throws CloudException, InternalException {
		return null;
	}

	@Override
	public void setDesiredCapacity(String scalingGroupId, int capacity) throws CloudException, InternalException {

	}

	@Override
	public String setTrigger(String name, String scalingGroupId, String statistic, String unitOfMeasure, String metric, int periodInSeconds, double lowerThreshold, double upperThreshold, int lowerIncrement, boolean lowerIncrementAbsolute, int upperIncrement, boolean upperIncrementAbsolute, int breachDuration) throws InternalException, CloudException {
		return null;
	}

	@Override
	public void updateAutoScalingGroup(String scalingGroupId, String launchConfigurationId, int minServers, int maxServers, int cooldown, String... zoneIds) throws InternalException, CloudException {

	}

}
