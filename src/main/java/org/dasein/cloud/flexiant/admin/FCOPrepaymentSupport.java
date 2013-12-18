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

import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.admin.Offering;
import org.dasein.cloud.admin.Prepayment;
import org.dasein.cloud.admin.PrepaymentSupport;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.identity.ServiceAction;

/**
 * The PrepaymentSupport implementation for the Dasein FCO implementation
 * 
 * This functionality is currently not supported by the FCO user service so this will not be available to the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOPrepaymentSupport implements PrepaymentSupport {

	@SuppressWarnings("unused")
	private FCOProvider provider;
	
	public FCOPrepaymentSupport(FCOProvider provider) {
		this.provider = provider;
	}

	@Override
	public @Nonnull
	String[] mapServiceAction(@Nonnull ServiceAction action) {
		return new String[0];
	}

	@Override
	public Offering getOffering(String offeringId) throws InternalException, CloudException {
		return null;
	}

	@Override
	public Prepayment getPrepayment(String prepaymentId) throws InternalException, CloudException {
		return null;
	}

	@Override
	public String getProviderTermForOffering(Locale locale) {
		return null;
	}

	@Override
	public String getProviderTermForPrepayment(Locale locale) {
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return false;
	}

	@Override
	public Collection<Offering> listOfferings() throws InternalException, CloudException {
		return null;
	}

	@Override
	public Collection<Prepayment> listPrepayments() throws InternalException, CloudException {
		return null;
	}

	@Override
	public String prepay(String offeringId, int count) throws InternalException, CloudException {
		return null;
	}
}