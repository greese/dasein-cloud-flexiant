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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.network.VPN;
import org.dasein.cloud.network.VPNConnection;
import org.dasein.cloud.network.VPNGateway;
import org.dasein.cloud.network.VPNProtocol;
import org.dasein.cloud.network.VPNSupport;

/**
 * The VPNSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOVPNSupport implements VPNSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCOVPNSupport(FCOProvider provider){
		this.provider = provider;
	}
	
	@Override
	public String[] mapServiceAction(ServiceAction action) {
		
		return null;
	}

	@Override
	public void attachToVLAN(String providerVpnId, String providerVlanId) throws CloudException, InternalException {
		

	}

	@Override
	public void connectToGateway(String providerVpnId, String toGatewayId) throws CloudException, InternalException {
		

	}

	@Override
	public VPN createVPN(String inProviderDataCenterId, String name, String description, VPNProtocol protocol) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public VPNGateway createVPNGateway(String endpoint, String name, String description, VPNProtocol protocol, String bgpAsn) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public void deleteVPN(String providerVpnId) throws CloudException, InternalException {
		

	}

	@Override
	public void deleteVPNGateway(String providerVPNGatewayId) throws CloudException, InternalException {
		

	}

	@Override
	public void detachFromVLAN(String providerVpnId, String providerVlanId) throws CloudException, InternalException {
		

	}

	@Override
	public void disconnectFromGateway(String providerVpnId, String fromGatewayId) throws CloudException, InternalException {
		

	}

	@Override
	public VPNGateway getGateway(String gatewayId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public VPN getVPN(String providerVpnId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Requirement getVPNDataCenterConstraint() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPNConnection> listGatewayConnections(String toGatewayId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listGatewayStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPNGateway> listGateways() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPNGateway> listGatewaysWithBgpAsn(String bgpAsn) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPNConnection> listVPNConnections(String toVpnId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listVPNStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPN> listVPNs() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<VPNProtocol> listSupportedVPNProtocols() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

}
