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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.platform.KeyValueDatabase;
import org.dasein.cloud.platform.KeyValueDatabaseSupport;
import org.dasein.cloud.platform.KeyValuePair;

/**
 * The KeyValueDatabaseSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOKeyValueDatabaseSupport implements KeyValueDatabaseSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCOKeyValueDatabaseSupport(FCOProvider provider){
		this.provider = provider;
	}
	
	@Override
	public String[] mapServiceAction(ServiceAction action) {
		
		return null;
	}

	@Override
	public void addKeyValuePairs(String inDatabaseId, String itemId, KeyValuePair... pairs) throws CloudException, InternalException {
		

	}

	@Override
	public String createDatabase(String name, String description) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<KeyValuePair> getKeyValuePairs(String inDatabaseId, String itemId, boolean consistentRead) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public KeyValueDatabase getDatabase(String databaseId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String getProviderTermForDatabase(Locale locale) {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public boolean isSupportsKeyValueDatabases() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public Iterable<String> list() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listKeyValueDatabaseStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Map<String, Set<KeyValuePair>> query(String queryString, boolean consistentRead) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public void removeKeyValuePairs(String inDatabaseId, String itemId, KeyValuePair... pairs) throws CloudException, InternalException {
		

	}

	@Override
	public void removeKeyValuePairs(String inDatabaseId, String itemId, String... keys) throws CloudException, InternalException {
		

	}

	@Override
	public void removeDatabase(String providerDatabaseId) throws CloudException, InternalException {
		

	}

	@Override
	public void replaceKeyValuePairs(String inDatabaseId, String itemId, KeyValuePair... pairs) throws CloudException, InternalException {
		

	}

}
