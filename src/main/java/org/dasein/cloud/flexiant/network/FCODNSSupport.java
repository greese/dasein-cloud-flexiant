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

import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.network.DNSRecord;
import org.dasein.cloud.network.DNSRecordType;
import org.dasein.cloud.network.DNSSupport;
import org.dasein.cloud.network.DNSZone;

/**
 * The DNSSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCODNSSupport implements DNSSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCODNSSupport(FCOProvider provider){
		this.provider = provider;
	}
	
	@Override
	public String[] mapServiceAction(ServiceAction action) {
		
		return null;
	}

	@Override
	public DNSRecord addDnsRecord(String providerDnsZoneId, DNSRecordType recordType, String name, int ttl, String... values) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String createDnsZone(String domainName, String name, String description) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public void deleteDnsRecords(DNSRecord... dnsRecords) throws CloudException, InternalException {
		

	}

	@Override
	public void deleteDnsZone(String providerDnsZoneId) throws CloudException, InternalException {
		

	}

	@Override
	public DNSZone getDnsZone(String providerDnsZoneId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String getProviderTermForRecord(Locale locale) {
		
		return null;
	}

	@Override
	public String getProviderTermForZone(Locale locale) {
		
		return null;
	}

	@Override
	public Iterable<DNSRecord> listDnsRecords(String providerDnsZoneId, DNSRecordType forType, String name) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listDnsZoneStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<DNSZone> listDnsZones() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

}
