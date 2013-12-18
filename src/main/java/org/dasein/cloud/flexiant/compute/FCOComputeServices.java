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

import javax.annotation.Nullable;

import org.dasein.cloud.compute.AbstractComputeServices;
import org.dasein.cloud.compute.AutoScalingSupport;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.SnapshotSupport;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VolumeSupport;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.compute.FCOMachineImageSupport;
import org.dasein.cloud.flexiant.compute.FCOSnapshotSupport;
import org.dasein.cloud.flexiant.compute.FCOVirtualMachineSupport;
import org.dasein.cloud.flexiant.compute.FCOVolumeSupport;

/**
 * The AbstractComputeServices implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOComputeServices extends AbstractComputeServices {

	private FCOProvider provider;

	public FCOComputeServices(FCOProvider provider) {
		this.provider = provider;
	}

	@Override
	public AutoScalingSupport getAutoScalingSupport() {
		return null;
	}

	@Override
	public MachineImageSupport getImageSupport() {
		return new FCOMachineImageSupport(provider);
	}

	@Override
	@Nullable
	public SnapshotSupport getSnapshotSupport() {
		return new FCOSnapshotSupport(provider);
	}

	@Override
	public VirtualMachineSupport getVirtualMachineSupport() {
		return new FCOVirtualMachineSupport(provider);
	}

	@Override
	public VolumeSupport getVolumeSupport() {
		return new FCOVolumeSupport(provider);
	}
}
