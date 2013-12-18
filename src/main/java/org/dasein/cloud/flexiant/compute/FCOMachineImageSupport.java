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
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.compute.AbstractImageSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.ImageFilterOptions;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;

import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.Image;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.UserService;

/**
 * The AbstractImageSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOMachineImageSupport extends AbstractImageSupport {

	private UserService userService = null;

	public FCOMachineImageSupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public MachineImage getImage(String providerImageId) throws CloudException, InternalException {
		try{
			return createMachineImageFromImage((Image) FCOProviderUtils.getResource(userService, providerImageId, ResourceType.IMAGE));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public String getProviderTermForImage(Locale locale, ImageClass cls) {
		return "Image";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public Iterable<MachineImage> listImages(ImageFilterOptions options) throws CloudException, InternalException {

		if(options.getImageClass().equals(ImageClass.KERNEL) || options.getImageClass().equals(ImageClass.RAMDISK)){
			return new ArrayList<MachineImage>();
		}

		List<MachineImage> list = new ArrayList<MachineImage>();
		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.IMAGE);

			if(result == null || result.getList() == null){
				return list;
			}

			Platform requestedPlatform = options.getPlatform();
			for(Object obj : result.getList()){
				MachineImage image = createMachineImageFromImage((Image) obj);

				if(requestedPlatform != null && !requestedPlatform.equals(Platform.UNKNOWN) && !image.getPlatform().equals(Platform.UNKNOWN)){
					if(!image.getPlatform().equals(requestedPlatform)){
						continue;
					}
				}

				list.add(image);
			}
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
		return list;
	}

	@Override
	public void remove(String providerImageId, boolean checkState) throws CloudException, InternalException {
		try{
			userService.deleteResource(providerImageId, checkState, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Iterable<MachineImage> listImages(@Nonnull ImageClass cls, @Nonnull String ownedBy) throws CloudException, InternalException {
		return listImages(ImageFilterOptions.getInstance(cls).withAccountNumber(ownedBy));
	}

	@Override
	@Nonnull
	public Iterable<MachineImage> listImages(@Nonnull ImageClass cls) throws CloudException, InternalException {
		return listImages(ImageFilterOptions.getInstance(cls));
	}

	@Override
	@Nonnull
	public Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
		if(cls.equals(ImageClass.KERNEL) || cls.equals(ImageClass.RAMDISK)){
			return new ArrayList<ResourceStatus>();
		}

		List<ResourceStatus> list = new ArrayList<ResourceStatus>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.IMAGE);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				Image image = (Image) obj;

				list.add(new ResourceStatus(image.getResourceUUID(), machineImageStateFromResourceState(image.getResourceState())));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	@Nonnull
	public Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
		List<ImageClass> list = new ArrayList<ImageClass>();

		list.add(ImageClass.MACHINE);

		return list;
	}

	// ---------- Helper Methods ---------- //

	private static MachineImageState machineImageStateFromResourceState(ResourceState resourceState) {
		switch(resourceState){
		case ACTIVE:
			return MachineImageState.ACTIVE;
		case CREATING:
			return MachineImageState.PENDING;
		case DELETED:
		default:
			return MachineImageState.DELETED;
		}
	}

	private static MachineImage createMachineImageFromImage(Image image) {

		if(image == null){
			return null;
		}

		MachineImage machineImage = null;

		MachineImageState state = machineImageStateFromResourceState(image.getResourceState());
		machineImage = MachineImage.getMachineImageInstance(image.getCustomerUUID(), image.getClusterUUID(), image.getResourceUUID(), state, image.getResourceName(), image.getResourceName(), Architecture.I32, Platform.guess(image.getResourceName()));
		machineImage.constrainedTo(image.getVdcUUID());
		if(image.getResourceCreateDate() != null){
			machineImage.createdAt(image.getResourceCreateDate().toGregorianCalendar().getTimeInMillis());
		}

		return machineImage;
	}
}
