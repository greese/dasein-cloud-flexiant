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

import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.platform.FCOCDNSupport;
import org.dasein.cloud.flexiant.platform.FCOKeyValueDatabaseSupport;
import org.dasein.cloud.flexiant.platform.FCOMQSupport;
import org.dasein.cloud.flexiant.platform.FCOMonitoringSupport;
import org.dasein.cloud.flexiant.platform.FCOPushNotificationSupport;
import org.dasein.cloud.flexiant.platform.FCORelationalDatabaseSupport;
import org.dasein.cloud.platform.AbstractPlatformServices;
import org.dasein.cloud.platform.CDNSupport;
import org.dasein.cloud.platform.KeyValueDatabaseSupport;
import org.dasein.cloud.platform.MQSupport;
import org.dasein.cloud.platform.MonitoringSupport;
import org.dasein.cloud.platform.PushNotificationSupport;
import org.dasein.cloud.platform.RelationalDatabaseSupport;

/**
 * The AbstractPlatformServices implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOPlatformServices extends AbstractPlatformServices {

	private FCOProvider provider;
	
	public FCOPlatformServices(FCOProvider provider) {
		this.provider = provider;
	}
	
	@Override
	public CDNSupport getCDNSupport() {
		return new FCOCDNSupport(provider);
	}
	@Override
	public KeyValueDatabaseSupport getKeyValueDatabaseSupport() {
		return new FCOKeyValueDatabaseSupport(provider);
	}
	@Override
	public MQSupport getMessageQueueSupport() {
		return new FCOMQSupport(provider);
	}
	@Override
	public MonitoringSupport getMonitoringSupport() {
		return new FCOMonitoringSupport(provider);
	}
	@Override
	public PushNotificationSupport getPushNotificationSupport() {
		return new FCOPushNotificationSupport(provider);
	}
	@Override
	public RelationalDatabaseSupport getRelationalDatabaseSupport() {
		return new FCORelationalDatabaseSupport(provider);
	}
}
