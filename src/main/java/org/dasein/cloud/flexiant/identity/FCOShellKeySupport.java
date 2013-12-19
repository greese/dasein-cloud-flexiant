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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.identity.SSHKeypair;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.identity.ShellKeySupport;

import java.util.ArrayList;

import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.SshKey;
import com.extl.jade.user.UserService;

/**
 * The ShellKeySupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOShellKeySupport implements ShellKeySupport {

	private FCOProvider provider;
	private UserService userService;
	
	public FCOShellKeySupport(FCOProvider provider){
		this.provider = provider;
		userService = FCOProviderUtils.getUserServiceFromContext(this.provider.getContext());
	}
	
	@Override
	public String[] mapServiceAction(ServiceAction action) {		
		return new String[0];
	}

	@Override
	public SSHKeypair createKeypair(String name) throws InternalException, CloudException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void deleteKeypair(String providerId) throws InternalException, CloudException {
		try {
			userService.deleteResource(providerId, true, null);
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}

	}

	@Override
	public String getFingerprint(String providerId) throws InternalException, CloudException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public Requirement getKeyImportSupport() throws CloudException, InternalException {
		return Requirement.REQUIRED;
	}

	@Override
	public SSHKeypair getKeypair(String providerId) throws InternalException, CloudException {
		SSHKeypair sshkeyPair = null;
		SearchFilter filter = new SearchFilter();		
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("resourceUUID");
		condition.getValue().add(providerId);
		filter.getFilterConditions().add(condition);	
		try {
			List<Object> sshkeys = FCOProviderUtils.listAllResources(userService, filter, ResourceType.SSHKEY).getList();
			if(sshkeys.size() > 0){
				SshKey sshkey = (SshKey)sshkeys.get(0);
				sshkeyPair = new SSHKeypair();
				sshkeyPair.setName(sshkey.getResourceName());
				sshkeyPair.setPublicKey(sshkey.getPublicKey());
				sshkeyPair.setProviderKeypairId(sshkey.getResourceUUID());
				sshkeyPair.setProviderOwnerId(sshkey.getCustomerUUID());
				sshkeyPair.setProviderRegionId(sshkey.getClusterUUID());
				
			}
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}
		return sshkeyPair;	
	}

	@Override
	public String getProviderTermForKeypair(Locale locale) {
		return "SSHKey";
	}

	@Override
	public SSHKeypair importKeypair(String name, String publicKey) throws InternalException, CloudException {
		SSHKeypair sshkeyPair = null;
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();		
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("resourceName");
		condition.getValue().add(name);
		filter.getFilterConditions().add(condition);
		condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("publicKey");
		condition.getValue().add(publicKey);
		filter.getFilterConditions().add(condition);
		try {
			List<Object> sshkeys = FCOProviderUtils.listAllResources(userService, filter, ResourceType.SSHKEY).getList();
			if(sshkeys.size() > 0){
				SshKey sshkey = (SshKey)sshkeys.get(0);
				sshkeyPair = new SSHKeypair();
				sshkeyPair.setName(sshkey.getResourceName());
				sshkeyPair.setPublicKey(sshkey.getPublicKey());
				sshkeyPair.setProviderKeypairId(sshkey.getResourceUUID());
				sshkeyPair.setProviderOwnerId(sshkey.getCustomerUUID());
				sshkeyPair.setProviderRegionId(sshkey.getClusterUUID());
				
			}
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}
		return sshkeyPair;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Collection<SSHKeypair> list() throws InternalException, CloudException {
		List<SSHKeypair> sshkeyList = new ArrayList<SSHKeypair>();
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("resourceState");
		condition.getValue().add(ResourceState.ACTIVE.name());
		filter.getFilterConditions().add(condition);
		try {
			List<Object> sshkeys = FCOProviderUtils.listAllResources(userService, filter, ResourceType.SSHKEY).getList();
			if(sshkeys.size() > 0){
				SshKey sshkey = null;
				for(Object obj : sshkeys){
					sshkey = (SshKey)obj;
					SSHKeypair keyPair = new SSHKeypair();
					keyPair.setName(sshkey.getResourceName());
					keyPair.setPublicKey(sshkey.getPublicKey());
					keyPair.setProviderKeypairId(sshkey.getResourceUUID());
					keyPair.setProviderOwnerId(sshkey.getCustomerUUID());
					keyPair.setProviderRegionId(sshkey.getClusterUUID());
					sshkeyList.add(keyPair);
				}
			}
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}
		return sshkeyList;
	}

}
