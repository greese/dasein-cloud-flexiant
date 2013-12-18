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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.dc.DataCenter;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.dc.Region;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;

import com.extl.jade.user.Cluster;
import com.extl.jade.user.Condition;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.QueryLimit;
import com.extl.jade.user.ResourceKey;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserService;
import com.extl.jade.user.Vdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The DataCenterServices implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCODataCenterServices implements DataCenterServices {

	private FCOProvider provider;
	private UserService userService = null;

	public FCODataCenterServices(FCOProvider provider) {
		this.provider = provider;
		this.userService = FCOProviderUtils.getUserServiceFromContext(this.provider.getContext());
	}

	@Override
	public String getProviderTermForDataCenter(Locale locale) {
		return "VDC";
	}

	@Override
	public String getProviderTermForRegion(Locale locale) {
		return "cluster";
	}

	@Override
	public DataCenter getDataCenter(String dataCenterID) throws InternalException, CloudException {
		if (dataCenterID == null || dataCenterID.isEmpty()) {
			return null;
		}
		
		SearchFilter filter = new SearchFilter();
		FilterCondition equalsUUID = new FilterCondition();
		equalsUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsUUID.setField("resourceUUID");
		equalsUUID.getValue().add(dataCenterID);
		filter.getFilterConditions().add(equalsUUID);
		
		QueryLimit limit = new QueryLimit();
		limit.setMaxRecords(1);
		
		try {
			ListResult result = userService.listResources(filter, limit, ResourceType.VDC);
			
			if(result != null && result.getList() != null && result.getList().size() > 0){
				return createDataCentreFromVDC((Vdc) result.getList().get(0));
			}
			
		} catch (ExtilityException e) {
			
		}

		return null;
	}

	@Override
	public Region getRegion(String regionID) throws InternalException, CloudException {
		if (regionID == null || regionID.isEmpty()) {
			return null;
		}

		SearchFilter filter = new SearchFilter();
		FilterCondition equalsUUID = new FilterCondition();
		equalsUUID.setCondition(Condition.IS_EQUAL_TO);
		equalsUUID.setField("resourceUUID");
		equalsUUID.getValue().add(regionID);
		filter.getFilterConditions().add(equalsUUID);
		
		QueryLimit limit = new QueryLimit();
		limit.setMaxRecords(1);
		
		try {
			ListResult result = userService.listResources(filter, limit, ResourceType.CLUSTER);
			
			if(result != null && result.getList() != null && result.getList().size() > 0){
				return createRegionFromCluster((Cluster) result.getList().get(0));
			}
			
		} catch (ExtilityException e) {
		}

		return null;
	}

	@Override
	public Collection<DataCenter> listDataCenters(String regionID) throws InternalException, CloudException {
		if (regionID == null || regionID.isEmpty()) {
			return null;
		}
		List<DataCenter> dataCenters = new ArrayList<DataCenter>();
		SearchFilter filter = new SearchFilter();
		FilterCondition fc = new FilterCondition();
		fc.setCondition(Condition.IS_EQUAL_TO);
		fc.setField("clusterUUID");
		fc.getValue().add(regionID);
		filter.getFilterConditions().add(fc);
		try {
			List<Object> vdcs = userService.listResources(filter, null, ResourceType.VDC).getList();
			for (Object obj : vdcs) {
				dataCenters.add(createDataCentreFromVDC((Vdc) obj));
			}
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}
		return dataCenters;
	}

	@Override
	public Collection<Region> listRegions() throws InternalException, CloudException {
		List<Region> regions = new java.util.ArrayList<Region>();
		try {
			List<Object> clusters = userService.listResources(null, null, ResourceType.CLUSTER).getList();
			if (clusters.size() > 0) {
				for (Object obj : clusters) {
					regions.add(createRegionFromCluster((Cluster) obj));
				}
			}
		} catch (ExtilityException e) {
			throw new CloudException(e);
		}
		return regions;
	}

	// ---------- Helper Methods ---------- //
	
	private static Region createRegionFromCluster(Cluster cluster){
		
		Region region = new Region();
		region.setActive(cluster.getResourceState() == ResourceState.ACTIVE ? true : false);
		region.setAvailable(cluster.getResourceState() == ResourceState.ACTIVE ? true : false);
		
		region.setJurisdiction("all");
		List<ResourceKey> keys = cluster.getResourceKey();
		if (keys.size() > 0)
			for (ResourceKey key : keys) {
				if (key.getName() != null && key.getName().equals("jurisidiction")) {
					region.setJurisdiction(key.getValue());
				}
			}
		region.setName(cluster.getResourceName());
		region.setProviderRegionId(cluster.getResourceUUID());
		
		return region;
		
	}
	private static DataCenter createDataCentreFromVDC(Vdc vdc){
		DataCenter dataCenter = new DataCenter();
		dataCenter.setActive(vdc.getResourceState() == ResourceState.ACTIVE ? true : false);
		dataCenter.setAvailable(vdc.getResourceState() == ResourceState.ACTIVE ? true : false);
		dataCenter.setName(vdc.getResourceName());
		dataCenter.setProviderDataCenterId(vdc.getResourceUUID());
		dataCenter.setRegionId(vdc.getClusterUUID());
		
		return dataCenter;
	}
	
}
