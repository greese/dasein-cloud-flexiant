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

package org.dasein.cloud.flexiant.platform;

import java.util.Collection;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.platform.CDNSupport;
import org.dasein.cloud.platform.Distribution;

/**
 * The CDNSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOCDNSupport implements CDNSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;

	public FCOCDNSupport(FCOProvider provider) {
		this.provider = provider;
	}

	@Override
	public String[] mapServiceAction(ServiceAction action) {
		return null;
	}

	@Override
	public String create(String origin, String name, boolean active, String... aliases) throws InternalException, CloudException {
		return null;
	}

	@Override
	public void delete(String distributionId) throws InternalException, CloudException {

	}

	@Override
	public Distribution getDistribution(String distributionId) throws InternalException, CloudException {
		return null;
	}

	@Override
	public String getProviderTermForDistribution(Locale locale) {
		return null;
	}

	@Override
	public boolean isSubscribed() throws InternalException, CloudException {

		return false;
	}

	@Override
	public Collection<Distribution> list() throws InternalException, CloudException {

		return null;
	}

	@Override
	public Iterable<ResourceStatus> listDistributionStatus() throws InternalException, CloudException {

		return null;
	}

	@Override
	public void update(String distributionId, String name, boolean active, String... aliases) throws InternalException, CloudException {

	}

}
