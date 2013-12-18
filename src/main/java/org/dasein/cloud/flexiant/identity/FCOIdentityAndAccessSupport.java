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

import java.util.ArrayList;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.identity.AccessKey;
import org.dasein.cloud.identity.CloudGroup;
import org.dasein.cloud.identity.CloudPermission;
import org.dasein.cloud.identity.CloudPolicy;
import org.dasein.cloud.identity.CloudUser;
import org.dasein.cloud.identity.IdentityAndAccessSupport;
import org.dasein.cloud.identity.ServiceAction;
import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Group;
import com.extl.jade.user.GroupType;
import com.extl.jade.user.Job;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Permission;
import com.extl.jade.user.QueryLimit;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserDetails;
import com.extl.jade.user.UserService;

/**
 * The IdentityAndAccessSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOIdentityAndAccessSupport implements IdentityAndAccessSupport {

	private FCOProvider provider;
	private UserService userService;

	public FCOIdentityAndAccessSupport(FCOProvider provider) {
		this.provider = provider;

		userService = FCOProviderUtils.getUserServiceFromContext(this.provider.getContext());
	}

	@Override
	public String[] mapServiceAction(ServiceAction action) {
		return new String[0];
	}

	@Override
	public void addUserToGroups(String providerUserId, String... providerGroupIds) throws CloudException, InternalException {
		for(String groupUUID : providerGroupIds){
			try{
				userService.addUserToGroup(providerUserId, groupUUID);
			}catch (ExtilityException e){
				throw new CloudException(e);
			}
		}
	}

	@Override
	public CloudGroup createGroup(String groupName, String path, boolean asAdminGroup) throws CloudException, InternalException {

		String[] pathSplit = path.split("/");

		Group group = new Group();
		group.setResourceName(groupName);
		group.setType(asAdminGroup ? GroupType.ADMIN : GroupType.NORMAL);
		group.setBillingEntityUUID(pathSplit[0]);
		group.setCustomerUUID(pathSplit[1]);

		try{
			Job job = userService.createGroup(group, null);
			userService.waitForJob(job.getResourceUUID(), false);

			group.setResourceUUID(job.getItemUUID());
			return createCloudGroupFromGroup(group);

		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public CloudUser createUser(String userName, String path, String... autoJoinGroupIds) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public AccessKey enableAPIAccess(String providerUserId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void enableConsoleAccess(String providerUserId, byte[] password) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public CloudGroup getGroup(String providerGroupId) throws CloudException, InternalException {
		try{
			return createCloudGroupFromGroup((Group) FCOProviderUtils.getResource(userService, providerGroupId, ResourceType.GROUP, false));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public CloudUser getUser(String providerUserId) throws CloudException, InternalException {
		try{
			return createCloudUserFromUser((UserDetails) FCOProviderUtils.getResource(userService, providerUserId, ResourceType.USER, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Iterable<CloudGroup> listGroups(String pathBase) throws CloudException, InternalException {

		ArrayList<CloudGroup> list = new ArrayList<CloudGroup>();
		SearchFilter filter = new SearchFilter();

		if(pathBase != null && !pathBase.isEmpty()){
			String[] pathSplit = pathBase.split("/");
			String beUUID = pathSplit[0];
			String custUUID = pathSplit.length > 1 ? pathSplit[1] : "";

			FilterCondition equalsBEUUID = new FilterCondition();
			equalsBEUUID.setCondition(Condition.IS_EQUAL_TO);
			equalsBEUUID.setField("billingEntityUUID");
			equalsBEUUID.getValue().add(beUUID);
			filter.getFilterConditions().add(equalsBEUUID);

			if(custUUID != null && !custUUID.isEmpty()){
				FilterCondition equalsCustUUID = new FilterCondition();
				equalsCustUUID.setCondition(Condition.IS_EQUAL_TO);
				equalsCustUUID.setField("customerUUID");
				equalsCustUUID.getValue().add(custUUID);
				filter.getFilterConditions().add(equalsCustUUID);
			}
		}

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.GROUP);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createCloudGroupFromGroup((Group) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<CloudGroup> listGroupsForUser(String providerUserId) throws CloudException, InternalException {

		SearchFilter filter = new SearchFilter();
		FilterCondition containsUser = new FilterCondition();
		containsUser.setCondition(Condition.CONTAINS);
		containsUser.setField("users");
		containsUser.getValue().add(providerUserId);
		filter.getFilterConditions().add(containsUser);

		ArrayList<CloudGroup> list = new ArrayList<CloudGroup>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.GROUP);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createCloudGroupFromGroup((Group) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<CloudPolicy> listPoliciesForGroup(String providerGroupId) throws CloudException, InternalException {

		ArrayList<CloudPolicy> list = new ArrayList<CloudPolicy>();

		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition permittedToResourceUUID = new FilterCondition();
			permittedToResourceUUID.setCondition(Condition.IS_EQUAL_TO);
			permittedToResourceUUID.setField("permittedTo.resourceUUID");
			permittedToResourceUUID.getValue().add(providerGroupId);
			filter.getFilterConditions().add(permittedToResourceUUID);
			FilterCondition permittedToResourceType = new FilterCondition();
			permittedToResourceType.setCondition(Condition.IS_EQUAL_TO);
			permittedToResourceType.setField("permittedTo.resourceType");
			permittedToResourceType.getValue().add(ResourceType.GROUP.name());
			filter.getFilterConditions().add(permittedToResourceType);

			ListResult result = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.PERMISSION);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createCloudPolicyFromPermission((Permission) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<CloudPolicy> listPoliciesForUser(String providerUserId) throws CloudException, InternalException {
		ArrayList<CloudPolicy> list = new ArrayList<CloudPolicy>();

		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition permittedToResourceUUID = new FilterCondition();
			permittedToResourceUUID.setCondition(Condition.IS_EQUAL_TO);
			permittedToResourceUUID.setField("permittedTo.resourceUUID");
			permittedToResourceUUID.getValue().add(providerUserId);
			filter.getFilterConditions().add(permittedToResourceUUID);
			FilterCondition permittedToResourceType = new FilterCondition();
			permittedToResourceType.setCondition(Condition.IS_EQUAL_TO);
			permittedToResourceType.setField("permittedTo.resourceType");
			permittedToResourceType.getValue().add(ResourceType.USER.name());
			filter.getFilterConditions().add(permittedToResourceType);

			ListResult result = FCOProviderUtils.listWithChildren(userService, filter, ResourceType.PERMISSION);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createCloudPolicyFromPermission((Permission) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<CloudUser> listUsersInGroup(String inProviderGroupId) throws CloudException, InternalException {

		ArrayList<CloudUser> list = new ArrayList<CloudUser>();

		try{
			Group group = (Group) FCOProviderUtils.getResource(userService, inProviderGroupId, ResourceType.GROUP, true);

			for(String userUUID : group.getUsers()){

				try{
					UserDetails user = (UserDetails) FCOProviderUtils.getResource(userService, userUUID, ResourceType.USER, true);

					if(user == null){
						continue;
					}

					list.add(createCloudUserFromUser(user));

				}catch (ExtilityException e){
					throw new CloudException(e);
				}

			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<CloudUser> listUsersInPath(String pathBase) throws CloudException, InternalException {

		ArrayList<CloudUser> list = new ArrayList<CloudUser>();

		SearchFilter filter = new SearchFilter();

		if(pathBase != null && !pathBase.isEmpty()){

			String[] pathSplit = pathBase.split("/");
			String beUUID = pathSplit[0];
			String custUUID = pathSplit.length > 1 ? pathSplit[1] : "";

			FilterCondition equalsBEUUID = new FilterCondition();
			equalsBEUUID.setCondition(Condition.IS_EQUAL_TO);
			equalsBEUUID.setField("billingEntityUUID");
			equalsBEUUID.getValue().add(beUUID);
			filter.getFilterConditions().add(equalsBEUUID);

			if(custUUID != null && !custUUID.isEmpty()){
				FilterCondition equalsCustUUID = new FilterCondition();
				equalsCustUUID.setCondition(Condition.IS_EQUAL_TO);
				equalsCustUUID.setField("customerUUID");
				equalsCustUUID.getValue().add(custUUID);
				filter.getFilterConditions().add(equalsCustUUID);
			}
		}

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.USER);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createCloudUserFromUser((UserDetails) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public void removeAccessKey(String sharedKeyPart, String providerUserId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void removeAccessKey(String sharedKeyPart) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void removeConsoleAccess(String providerUserId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void removeGroup(String providerGroupId) throws CloudException, InternalException {
		try{
			Job job = userService.deleteResource(providerGroupId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void removeGroupPolicy(String providerGroupId, String providerPolicyId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void removeUser(String providerUserId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void removeUserFromGroup(String providerUserId, String providerGroupId) throws CloudException, InternalException {

		try{
			Group group = (Group) FCOProviderUtils.getResource(userService, providerGroupId, ResourceType.GROUP, true);
			group.getUsers().remove(providerUserId);
			Job job = userService.modifyGroup(group, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void removeUserPolicy(String providerUserId, String providerPolicyId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void saveGroup(String providerGroupId, String newGroupName, String newPath) throws CloudException, InternalException {
		SearchFilter filter = new SearchFilter();
		FilterCondition equalsGroupUUID = new FilterCondition();
		equalsGroupUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsGroupUUID.setField("resourceUUID");
		equalsGroupUUID.getValue().add(providerGroupId);
		filter.getFilterConditions().add(equalsGroupUUID);

		QueryLimit limit = new QueryLimit();
		limit.setMaxRecords(1);

		try{
			ListResult result = userService.listResources(filter, limit, ResourceType.GROUP);

			if(result == null || result.getList() == null || result.getList().size() == 0){
				return;
			}

			Group group = (Group) result.getList().get(0);

			if(newGroupName != null && !newGroupName.isEmpty()){
				group.setResourceName(newGroupName);
			}
			if(newPath != null && !newPath.isEmpty()){
				String[] split = newPath.split("/");
				if(split.length > 1){
					group.setCustomerUUID(split[1]);
				}
			}

			userService.modifyGroup(group, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public String[] saveGroupPolicy(String providerGroupId, String name, CloudPermission permission, ServiceAction action, String resourceId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public String[] saveUserPolicy(String providerUserId, String name, CloudPermission permission, ServiceAction action, String resourceId) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public void saveUser(String providerUserId, String newUserName, String newPath) throws CloudException, InternalException {
		throw new OperationNotSupportedException("This operation is not supported in " + this.provider.getProviderName());
	}

	@Override
	public boolean supportsAccessControls() throws CloudException, InternalException {
		return false;
	}

	@Override
	public boolean supportsConsoleAccess() throws CloudException, InternalException {
		return false;
	}

	@Override
	public boolean supportsAPIAccess() throws CloudException, InternalException {
		return false;
	}

	// ---------- Helper Methods ---------- //

	private static CloudGroup createCloudGroupFromGroup(Group group) {

		if(group == null){
			return null;
		}

		CloudGroup cloudGroup = new CloudGroup();

		cloudGroup.setName(group.getResourceName());
		cloudGroup.setPath(group.getBillingEntityUUID() + "/" + group.getCustomerUUID());
		cloudGroup.setProviderGroupId(group.getResourceUUID());
		cloudGroup.setProviderOwnerId(group.getCustomerUUID());

		return cloudGroup;
	}

	private static CloudUser createCloudUserFromUser(UserDetails user) {

		if(user == null){
			return null;
		}

		CloudUser cloudUser = new CloudUser();

		cloudUser.setPath(user.getBillingEntityUUID() + "/");
		cloudUser.setProviderOwnerId(user.getBillingEntityUUID());
		cloudUser.setProviderUserId(user.getResourceUUID());
		cloudUser.setUserName(user.getResourceName());

		return cloudUser;

	}

	private static CloudPolicy createCloudPolicyFromPermission(Permission permission){
		String policyId = permission.getPermittedResource().getResourceUUID() + "--" + permission.getPermittedTo().getResourceUUID() + "--" + permission.getCapability().name() + "--" + permission.getResourceType().name();
		String name = (permission.isPermitted() ? "Allow" : "Deny") + " " + permission.getCapability().name() + " " + permission.getResourceType().name();
		CloudPermission cloudPermission = permission.isPermitted() ? CloudPermission.ALLOW : CloudPermission.DENY;
		ServiceAction action = new ServiceAction(permission.getCapability().name() + "--" + permission.getResourceType().name());
		String resourceId = permission.getPermittedResource().getResourceUUID();

		return CloudPolicy.getInstance(policyId, name, cloudPermission, action, resourceId);
	}
	
}
