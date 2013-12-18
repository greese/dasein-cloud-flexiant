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
import org.dasein.cloud.DataFormat;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.platform.EndpointType;
import org.dasein.cloud.platform.PushNotificationSupport;
import org.dasein.cloud.platform.Subscription;
import org.dasein.cloud.platform.Topic;

/**
 * The PushNotificationSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOPushNotificationSupport implements PushNotificationSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCOPushNotificationSupport(FCOProvider provider){
		this.provider = provider;
	}
	
	@Override
	public String[] mapServiceAction(ServiceAction action) {
		
		return null;
	}

	@Override
	public String confirmSubscription(String providerTopicId, String token, boolean authenticateUnsubscribe) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Topic createTopic(String name) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Topic getTopic(String providerTopicId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String getProviderTermForSubscription(Locale locale) {
		
		return null;
	}

	@Override
	public String getProviderTermForTopic(Locale locale) {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public Collection<Subscription> listSubscriptions(String optionalTopicId) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<ResourceStatus> listTopicStatus() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Collection<Topic> listTopics() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public String publish(String providerTopicId, String subject, String message) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public void removeTopic(String providerTopicId) throws CloudException, InternalException {
		

	}

	@Override
	public void subscribe(String providerTopicId, EndpointType endpointType, DataFormat dataFormat, String endpoint) throws CloudException, InternalException {
		

	}

	@Override
	public void unsubscribe(String providerSubscriptionId) throws CloudException, InternalException {
		

	}

}
