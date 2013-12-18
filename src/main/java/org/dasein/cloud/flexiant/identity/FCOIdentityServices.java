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

package org.dasein.cloud.flexiant.identity;

import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.identity.FCOIdentityAndAccessSupport;
import org.dasein.cloud.flexiant.identity.FCOShellKeySupport;
import org.dasein.cloud.identity.AbstractIdentityServices;
import org.dasein.cloud.identity.IdentityAndAccessSupport;
import org.dasein.cloud.identity.ShellKeySupport;

/**
 * The FCOIdentityServices implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOIdentityServices extends AbstractIdentityServices {

	private FCOProvider provider;
	
	public FCOIdentityServices(FCOProvider provider){
		this.provider = provider;
	}
	
	@Override
	public IdentityAndAccessSupport getIdentityAndAccessSupport() {
		return new FCOIdentityAndAccessSupport(provider);
	}
	@Override
	public ShellKeySupport getShellKeySupport() {
		return new FCOShellKeySupport(provider);
	}
	
	
}