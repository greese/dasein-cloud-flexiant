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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.platform.AbstractMQSupport;
import org.dasein.cloud.platform.MQMessageIdentifier;
import org.dasein.cloud.platform.MQMessageReceipt;
import org.dasein.cloud.platform.MessageQueue;
import org.dasein.util.uom.time.Second;
import org.dasein.util.uom.time.TimePeriod;

/**
 * The AbstractMQSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOMQSupport extends AbstractMQSupport<FCOProvider> {

	public FCOMQSupport(FCOProvider provider) {
		super(provider);
	}

	@Override
	public String getProviderTermForMessageQueue(Locale locale) {
		
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		
		return false;
	}

	@Override
	public Iterable<MessageQueue> listMessageQueues() throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public Iterable<MQMessageReceipt> receiveMessages(String mqId, TimePeriod<Second> waitTime, int count, TimePeriod<Second> visibilityTimeout) throws CloudException, InternalException {
		
		return null;
	}

	@Override
	public MQMessageIdentifier sendMessage(String mqId, String message) throws CloudException, InternalException {
		
		return null;
	}

}
