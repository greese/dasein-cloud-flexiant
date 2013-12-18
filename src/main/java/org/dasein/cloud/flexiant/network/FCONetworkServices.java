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

import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.network.FCOFirewallSupport;
import org.dasein.cloud.flexiant.network.FCOVLANSupport;
import org.dasein.cloud.network.AbstractNetworkServices;
import org.dasein.cloud.network.DNSSupport;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.LoadBalancerSupport;
import org.dasein.cloud.network.NetworkFirewallSupport;
import org.dasein.cloud.network.VLANSupport;
import org.dasein.cloud.network.VPNSupport;

/**
 * The FCONetworkServices implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCONetworkServices extends AbstractNetworkServices {

	private FCOProvider provider;
	
	public FCONetworkServices(FCOProvider provider) {
		this.provider = provider;
	}
	
	@Override
	public DNSSupport getDnsSupport() {
		return null;
	}
	@Override
	public FirewallSupport getFirewallSupport() {
		return new FCOFirewallSupport(provider);
	}
	@Override
	public IpAddressSupport getIpAddressSupport() {
		return null;
	}
	@Override
	public LoadBalancerSupport getLoadBalancerSupport() {
		return null;
	}
	@Override
	public NetworkFirewallSupport getNetworkFirewallSupport() {
		return null;
	}
	@Override
	public VLANSupport getVlanSupport() {
		return new FCOVLANSupport(provider);
	}
	@Override
	public VPNSupport getVpnSupport() {
		return null;
	}
}
