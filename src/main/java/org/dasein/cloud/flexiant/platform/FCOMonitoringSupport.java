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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.platform.AbstractMonitoringSupport;

/**
 * The AbstractMonitoringSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOMonitoringSupport extends AbstractMonitoringSupport {

	public FCOMonitoringSupport(FCOProvider provider) {
		super(provider);
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return false;
	}

}
