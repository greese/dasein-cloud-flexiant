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

package org.dasein.cloud.flexiant;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.dasein.cloud.ProviderContext;

import com.extl.jade.user.BillingPeriod;
import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Job;
import com.extl.jade.user.JobStatus;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Network;
import com.extl.jade.user.OrderedField;
import com.extl.jade.user.QueryLimit;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.ResultOrder;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserService;
import com.extl.jade.user.Vdc;

/**
 * Helper methods used throughout the implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public final class FCOProviderUtils {

	private static final int MAX_RECORDS = 50;
	public static final String DASEIN_FIREWALL_KEY = "Dasein Firewall";

	private FCOProviderUtils() {
	}

	public static ListResult listAllResources(UserService userService, SearchFilter filter, ResourceType resourceType) throws ExtilityException {		
		return listAllResources(userService, filter, resourceType, false);
	}
	
	public static ListResult listWithChildren(UserService userService, SearchFilter filter, ResourceType resourceType) throws ExtilityException {
		return listAllResources(userService, filter, resourceType, true);
	}
	
	private  static ListResult listAllResources(UserService userService, SearchFilter filter, ResourceType resourceType,boolean loadChildren) throws ExtilityException {
		ListResult result = new ListResult();
		QueryLimit limit = new QueryLimit();
		limit.setFrom(0);
		limit.setTo(MAX_RECORDS);
		limit.setLoadChildren(loadChildren);

		OrderedField orderedField = new OrderedField();
		orderedField.setSortOrder(ResultOrder.ASC);
		orderedField.setFieldName((resourceType == ResourceType.PERMISSION)? "permittedTo.resourceUUID":"resourceUUID");
		limit.getOrderBy().add(orderedField);

		if(resourceType == ResourceType.PERMISSION){
			// List resources does not work for the permission type in the user service
			result = userService.listPermissions(filter, limit);
		}else{
			result = userService.listResources(filter, limit, resourceType);
		}

		while (result.getList().size() != result.getTotalCount()){

			limit.setFrom(result.getListTo() + 1);
			limit.setTo((int) (result.getTotalCount() < result.getListTo() + (1 + MAX_RECORDS) ? result.getTotalCount() : result.getListTo() + (1 + MAX_RECORDS)));

			ListResult newResult = null;
			if(resourceType == ResourceType.PERMISSION){
				// List resources does not work for the permission type in the user service
				newResult = userService.listPermissions(filter, limit);
			}else{
				newResult = userService.listResources(filter, limit, resourceType);
			}

			if(newResult == null || newResult.getList() == null || newResult.getList().size() == 0){
				break;
			}

			result.getList().addAll(newResult.getList());
			result.setListTo((int) (result.getList().size() - 1));
		}

		return result;
	}
	
	public static Object getResource(UserService userService, String resourceUUID, ResourceType resourceType) throws ExtilityException {

		return getResource(userService, resourceUUID, resourceType, false);
	}
	public static Object getResource(UserService userService, String resourceUUID, ResourceType resourceType, boolean loadChildren) throws ExtilityException {

		ListResult result = new ListResult();
		QueryLimit limit = new QueryLimit();
		limit.setMaxRecords(1);
		limit.setLoadChildren(loadChildren);

		SearchFilter filter = new SearchFilter();

		FilterCondition isActive = new FilterCondition();
		isActive.setCondition(Condition.IS_EQUAL_TO);
		isActive.setField("resourceState");
		isActive.getValue().add(ResourceState.ACTIVE.name());
		filter.getFilterConditions().add(isActive);

		FilterCondition equalsUUID = new FilterCondition();
		equalsUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsUUID.setField("resourceUUID");
		equalsUUID.getValue().add(resourceUUID);
		filter.getFilterConditions().add(equalsUUID);

		result = userService.listResources(filter, limit, resourceType);

		if(result == null || result.getList() == null || result.getList().size() == 0){
			return null;
		}

		return result.getList().get(0);
	}

	public static UserService getUserServiceFromContext(ProviderContext context) {

		UserAPIService userAPI;
		try{
			userAPI = new UserAPIService(new URL(context.getEndpoint()), new QName("http://extility.flexiant.net", "UserAPI"));

			UserService userService = userAPI.getUserServicePort();
			BindingProvider bindingProvider = (BindingProvider) userService;

			bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, context.getEndpoint().toString());
			bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, new String(context.getAccessPublic()) + "/" + context.getAccountNumber());
			bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(context.getAccessPrivate()));

			return userService;
		}catch (MalformedURLException e){
		}

		return null;
	}

	public static boolean isSet(String value) {
		return (value != null && !value.isEmpty());
	}
	public static boolean isSet(Object[] value) {
		return value != null && value.length > 0;
	}

	public static String getDefaultVDC(UserService userService, String clusterUUID, String customerUUID) {

		if(!isSet(clusterUUID) || !isSet(customerUUID)){
			return null;
		}

		SearchFilter filter = new SearchFilter();
		FilterCondition equalsCustomerUUID = new FilterCondition();
		equalsCustomerUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsCustomerUUID.setField("customerUUID");
		equalsCustomerUUID.getValue().add(customerUUID);
		filter.getFilterConditions().add(equalsCustomerUUID);
		FilterCondition equalsClusterUUID = new FilterCondition();
		equalsClusterUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsClusterUUID.setField("clusterUUID");
		equalsClusterUUID.getValue().add(clusterUUID);
		filter.getFilterConditions().add(equalsClusterUUID);

		try{
			ListResult result = userService.listResources(filter, null, ResourceType.VDC);

			if(result == null || result.getList() == null){
				return null;
			}

			String vdcUUID = null;
			for(Object obj : result.getList()){
				Vdc vdc = (Vdc) obj;

				vdcUUID = vdc.getResourceUUID();

				if(vdc.getResourceName().contains("Default cluster")){
					break;
				}

			}

			return vdcUUID;

		}catch (ExtilityException e){
			return null;
		}

	}
	public static String getDefaultNetwork(UserService userService, String clusterUUID, String customerUUID) {

		if(!isSet(clusterUUID) || !isSet(customerUUID)){
			return null;
		}

		SearchFilter filter = new SearchFilter();
		FilterCondition equalsCustomerUUID = new FilterCondition();
		equalsCustomerUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsCustomerUUID.setField("customerUUID");
		equalsCustomerUUID.getValue().add(customerUUID);
		filter.getFilterConditions().add(equalsCustomerUUID);
		FilterCondition equalsClusterUUID = new FilterCondition();
		equalsClusterUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsClusterUUID.setField("clusterUUID");
		equalsClusterUUID.getValue().add(clusterUUID);
		filter.getFilterConditions().add(equalsClusterUUID);

		try{
			ListResult result = userService.listResources(filter, null, ResourceType.NETWORK);

			if(result == null || result.getList() == null){
				return null;
			}

			String networkUUID = null;
			for(Object obj : result.getList()){
				Network network = (Network) obj;

				networkUUID = network.getResourceUUID();

				if(network.getResourceName().contains("Default network")){
					break;
				}

			}

			return networkUUID;

		}catch (ExtilityException e){
			return null;
		}

	}

	/**
	 * Used to convert dasein metadata map into Jade metadata xml
	 */
	public static String convertMapToXML(Map<String, String> metaData) {

		String toReturn = "";

		for(String key : metaData.keySet()){
			toReturn += String.format("<%s>%s</%s>", key, metaData.get(key), key);
		}

		return toReturn;
	}

	public static Job waitForJob(UserService userService, String jobUUID, long timeout) throws InterruptedException, ExtilityException {
		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("resourceUUID");
		condition.getValue().add(jobUUID);
		filter.getFilterConditions().add(condition);
		if(System.currentTimeMillis() < timeout){
			List<Object> jobs = FCOProviderUtils.listAllResources(userService, filter, ResourceType.JOB).getList();
			if(jobs.size() > 0){
				Job job = (Job) jobs.get(0);
				if(job.getStatus() == JobStatus.CANCELLED || job.getStatus() == JobStatus.SUCCESSFUL || job.getStatus() == JobStatus.SUSPENDED || job.getStatus() == JobStatus.FAILED){
					return job;
				}else{
					Thread.sleep(5000);
					waitForJob(userService, jobUUID, timeout);
				}
			}
		}
		return null;
	}
	
	public static double getHourlyRate(BillingPeriod billingPeriod, double rate){
		
		switch(billingPeriod){
		case ANNUALLY:
			return rate / 8760.0;
		case DAILY:
			return rate / 24.0;		
		case MONTHLY:
			return rate / 672.0;		
		case WEEKLY:
			return rate / 168.0;
		case ONE_OFF:
			return 0.0;
		case HOURLY:
		default:
			return rate;

		}
	}
}