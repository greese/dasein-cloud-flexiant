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

package org.dasein.cloud.flexiant.compute;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.compute.AbstractSnapshotSupport;
import org.dasein.cloud.compute.Snapshot;
import org.dasein.cloud.compute.SnapshotCreateOptions;
import org.dasein.cloud.compute.SnapshotFilterOptions;
import org.dasein.cloud.compute.SnapshotState;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;

import com.extl.jade.user.Disk;
import com.extl.jade.user.Job;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SnapshotType;
import com.extl.jade.user.UserService;

/**
 * The AbstractSnapshotSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOSnapshotSupport extends AbstractSnapshotSupport {

	private UserService userService;

	public FCOSnapshotSupport(FCOProvider provider) {
		super(provider);

		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public String getProviderTermForSnapshot(Locale locale) {
		return "Snapshot";
	}

	@Override
	public boolean isSubscribed() throws InternalException, CloudException {
		return true;
	}

	@Override
	public Iterable<Snapshot> listSnapshots() throws InternalException, CloudException {

		UserService userService = FCOProviderUtils.getUserServiceFromContext(getContext());
		ArrayList<Snapshot> arraylist = new ArrayList<Snapshot>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.SNAPSHOT);
			if(result == null || result.getList() == null){
				return arraylist;
			}

			for(Object obj : result.getList()){
				arraylist.add(createSnapshotFromSnapshot(userService,(com.extl.jade.user.Snapshot) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return arraylist;
	}

	@Override
	@Nullable
	public String createSnapshot(@Nonnull SnapshotCreateOptions options) throws CloudException, InternalException {

		if(!FCOProviderUtils.isSet(options.getVolumeId())){
			if(FCOProviderUtils.isSet(options.getSnapshotId())){
				
				try{
					Job job = userService.cloneResource(options.getSnapshotId(), options.getName(), null, null);
					userService.waitForJob(job.getResourceUUID(), true);
					
					return job.getItemUUID();
				}catch (ExtilityException e){
					throw new CloudException(e);
				}
			}
			
			return null;
		}
		
		com.extl.jade.user.Snapshot newSnapshot = new com.extl.jade.user.Snapshot();

		newSnapshot.setResourceName(options.getName());
		newSnapshot.setClusterUUID(options.getRegionId());
		newSnapshot.setParentUUID(options.getVolumeId());
		newSnapshot.setType(SnapshotType.DISK);
		
		try{
			Job job = userService.createSnapshot(newSnapshot, null);
			userService.waitForJob(job.getResourceUUID(), true);
			
			return job.getItemUUID();
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nullable
	public Snapshot getSnapshot(@Nonnull String snapshotId) throws InternalException, CloudException {
		try{
			return createSnapshotFromSnapshot(userService, (com.extl.jade.user.Snapshot) FCOProviderUtils.getResource(userService, snapshotId, ResourceType.SNAPSHOT));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Iterable<Snapshot> listSnapshots(SnapshotFilterOptions options) throws InternalException, CloudException {
		return listSnapshots();
	}
	@Override
	public void remove(@Nonnull String snapshotId) throws InternalException, CloudException {
		try{
			Job job = userService.deleteResource(snapshotId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}
	@Override
	@Nonnull
	public 
	Iterable<Snapshot> searchSnapshots(@Nonnull SnapshotFilterOptions options) throws InternalException, CloudException {
		return listSnapshots();
	}
	
	@Override
	public @Nonnull
	Iterable<ResourceStatus> listSnapshotStatus() throws InternalException, CloudException {
		ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
		
		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.SNAPSHOT);
			
			if(result == null || result.getList() == null){
				return list;
			}
			
			for(Object obj : result.getList()){
				com.extl.jade.user.Snapshot snapshot = (com.extl.jade.user.Snapshot)obj;
				
				list.add(new ResourceStatus(snapshot.getResourceUUID(), resourceStateToSnapshotState(snapshot.getResourceState())));
			}
			
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		
		return list;
	}
	
	@Override
	public boolean supportsSnapshotCopying() throws CloudException, InternalException {
		return true;
	}
	@Override
	public boolean supportsSnapshotCreation() throws CloudException, InternalException {
		return true;
	}
	@Override
	public boolean supportsSnapshotSharing() throws InternalException, CloudException {
		return false;
	}
	@Override
	public boolean supportsSnapshotSharingWithPublic() throws InternalException, CloudException {
		return false;
	}

	// ---------- Helper methods ---------- //

	private static SnapshotState resourceStateToSnapshotState(ResourceState resourceState) {

		switch(resourceState){
		case ACTIVE:
			return SnapshotState.AVAILABLE;
		case CREATING:
			return SnapshotState.PENDING;
		case HIDDEN:
		case LOCKED:
		case DELETED:
		case TO_BE_DELETED:
		default:
			return SnapshotState.DELETED;
		}
	}

	private static Snapshot createSnapshotFromSnapshot(UserService userService, com.extl.jade.user.Snapshot jadeSnapshot) {

		if(jadeSnapshot == null){
			return null;
		}
		
		Snapshot daseinSnapshot = new Snapshot();
		daseinSnapshot.setCurrentState(resourceStateToSnapshotState(jadeSnapshot.getResourceState()));
		daseinSnapshot.setDescription(jadeSnapshot.getResourceName());
		daseinSnapshot.setName(jadeSnapshot.getResourceName());
		daseinSnapshot.setOwner(jadeSnapshot.getCustomerUUID());
		daseinSnapshot.setProviderSnapshotId(jadeSnapshot.getResourceUUID());
		daseinSnapshot.setRegionId(jadeSnapshot.getClusterUUID());
		daseinSnapshot.setSnapshotTimestamp(jadeSnapshot.getResourceCreateDate().toGregorianCalendar().getTimeInMillis());
		daseinSnapshot.setVolumeId(jadeSnapshot.getParentUUID());
		
		try{
			Object obj = FCOProviderUtils.getResource(userService, jadeSnapshot.getParentUUID(), ResourceType.DISK);
			if(obj instanceof Disk){
				daseinSnapshot.setSizeInGb((int) ((Disk) obj).getSize());
			}else{
				daseinSnapshot.setSizeInGb(20);
			}
		}catch (ExtilityException e){
			daseinSnapshot.setSizeInGb(20);
		}
		
		return daseinSnapshot;
	}
}
