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
import org.dasein.cloud.TimeWindow;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.platform.ConfigurationParameter;
import org.dasein.cloud.platform.Database;
import org.dasein.cloud.platform.DatabaseConfiguration;
import org.dasein.cloud.platform.DatabaseEngine;
import org.dasein.cloud.platform.DatabaseProduct;
import org.dasein.cloud.platform.DatabaseSnapshot;
import org.dasein.cloud.platform.RelationalDatabaseSupport;

/**
 * The RelationalDatabaseSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCORelationalDatabaseSupport implements RelationalDatabaseSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCORelationalDatabaseSupport(FCOProvider provider) {
		this.provider = provider;
	}

	@Override
	public String[] mapServiceAction(ServiceAction action) {
		
		return null;
	}

	@Override
	public void addAccess(String providerDatabaseId, String sourceCidr) throws CloudException, InternalException {
		

	}

	@Override
	public void alterDatabase(String providerDatabaseId, boolean applyImmediately, String productSize, int storageInGigabytes, String configurationId, String newAdminUser, String newAdminPassword, int newPort, int snapshotRetentionInDays, TimeWindow preferredMaintenanceWindow, TimeWindow preferredBackupWindow) throws CloudException, InternalException {
		

	}

	@Override
	public String createFromScratch(String dataSourceName, DatabaseProduct product, String databaseVersion, String withAdminUser, String withAdminPassword, int hostPort) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String createFromLatest(String dataSourceName, String providerDatabaseId, String productSize, String providerDataCenterId, int hostPort) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public String createFromSnapshot(String dataSourceName, String providerDatabaseId, String providerDbSnapshotId, String productSize, String providerDataCenterId, int hostPort) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String createFromTimestamp(String dataSourceName, String providerDatabaseId, long beforeTimestamp, String productSize, String providerDataCenterId, int hostPort) throws InternalException, CloudException {
		
		return null;
	}

	@Override
	public DatabaseConfiguration getConfiguration(String providerConfigurationId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Database getDatabase(String providerDatabaseId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<DatabaseEngine> getDatabaseEngines() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String getDefaultVersion(DatabaseEngine forEngine) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<String> getSupportedVersions(DatabaseEngine forEngine) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<DatabaseProduct> getDatabaseProducts(DatabaseEngine forEngine) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String getProviderTermForDatabase(Locale locale) {
		
		return null;
	}

	@Override
	public String getProviderTermForSnapshot(Locale locale) {
		
		return null;
	}

	@Override
	public DatabaseSnapshot getSnapshot(String providerDbSnapshotId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public boolean isSupportsFirewallRules() {
		
		return false;
	}

	@Override
	public boolean isSupportsHighAvailability() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public boolean isSupportsLowAvailability() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public boolean isSupportsMaintenanceWindows() {
		
		return false;
	}

	@Override
	public boolean isSupportsSnapshots() {
		
		return false;
	}

	@Override
	public Iterable<String> listAccess(String toProviderDatabaseId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<DatabaseConfiguration> listConfigurations() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listDatabaseStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<Database> listDatabases() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Collection<ConfigurationParameter> listParameters(String forProviderConfigurationId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<DatabaseSnapshot> listSnapshots(String forOptionalProviderDatabaseId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public void removeConfiguration(String providerConfigurationId) throws CloudException, InternalException {
		

	}

	@Override
	public void removeDatabase(String providerDatabaseId) throws CloudException, InternalException {
		

	}

	@Override
	public void removeSnapshot(String providerSnapshotId) throws CloudException, InternalException {
		

	}

	@Override
	public void resetConfiguration(String providerConfigurationId, String... parameters) throws CloudException, InternalException {
		

	}

	@Override
	public void restart(String providerDatabaseId, boolean blockUntilDone) throws CloudException, InternalException {
		

	}

	@Override
	public void revokeAccess(String providerDatabaseId, String sourceCide) throws CloudException, InternalException {
		

	}

	@Override
	public void updateConfiguration(String providerConfigurationId, ConfigurationParameter... parameters) throws CloudException, InternalException {
		

	}

	@Override
	public DatabaseSnapshot snapshot(String providerDatabaseId, String name) throws CloudException, InternalException {
		
		return null;
	}

}
