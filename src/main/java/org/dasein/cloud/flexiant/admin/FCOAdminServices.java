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

package org.dasein.cloud.flexiant.admin;

import org.dasein.cloud.admin.AbstractAdminServices;
import org.dasein.cloud.admin.PrepaymentSupport;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.admin.FCOPrepaymentSupport;

/**
 * The AbstractAdminServices implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOAdminServices extends AbstractAdminServices {

	private FCOProvider provider;
	
	public FCOAdminServices(FCOProvider provider) {
		this.provider = provider;
	}
	
	@Override
	public PrepaymentSupport getPrepaymentSupport() {
		return new FCOPrepaymentSupport(provider);
	}
}