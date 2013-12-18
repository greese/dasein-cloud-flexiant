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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.compute.AbstractVolumeSupport;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.Volume;
import org.dasein.cloud.compute.VolumeCreateOptions;
import org.dasein.cloud.compute.VolumeFilterOptions;
import org.dasein.cloud.compute.VolumeFormat;
import org.dasein.cloud.compute.VolumeProduct;
import org.dasein.cloud.compute.VolumeState;
import org.dasein.cloud.compute.VolumeType;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.util.uom.storage.Gigabyte;
import org.dasein.util.uom.storage.Storage;

import com.extl.jade.user.Condition;
import com.extl.jade.user.Disk;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Job;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.ProductComponent;
import com.extl.jade.user.ProductOffer;
import com.extl.jade.user.ResourceState;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserService;
import com.extl.jade.user.Value;

/**
 * The AbstractVolumeSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOVolumeSupport extends AbstractVolumeSupport {

	UserService userService;

	public FCOVolumeSupport(FCOProvider provider) {
		super(provider);

		this.userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public Storage<Gigabyte> getMinimumVolumeSize() throws InternalException, CloudException {
		Storage<Gigabyte> minimum = new Storage<Gigabyte>();
		minimum.setQuantity(20);
		minimum.setUnitOfMeasure(new Gigabyte());

		return minimum;
	}

	@Override
	public Storage<Gigabyte> getMaximumVolumeSize() throws InternalException, CloudException {
		Storage<Gigabyte> maximum = new Storage<Gigabyte>();
		maximum.setQuantity(1000);
		maximum.setUnitOfMeasure(new Gigabyte());

		return maximum;
	}

	@Override
	public String getProviderTermForVolume(Locale locale) {
		return "Disk";
	}

	@Override
	public Iterable<String> listPossibleDeviceIds(Platform platform) throws InternalException, CloudException {

		ArrayList<String> list = new ArrayList<String>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.DISK);
			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(((Disk) obj).getResourceUUID());

			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public Iterable<Volume> listVolumes() throws InternalException, CloudException {

		ArrayList<Volume> list = new ArrayList<Volume>();

		try{
			SearchFilter filter = new SearchFilter();
			FilterCondition condition = new FilterCondition();
			condition.setCondition(Condition.IS_EQUAL_TO);
			condition.setField("resourceState");
			condition.getValue().add(ResourceState.ACTIVE.name());
			filter.getFilterConditions().add(condition);

			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DISK);
			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createVolumeFromDisk((Disk) obj));

			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	@Nonnull
	public String createVolume(@Nonnull VolumeCreateOptions options) throws InternalException, CloudException {

		if(options.getFormat() == VolumeFormat.NFS){
			throw new OperationNotSupportedException("Creating NFS volumes is not supported by the provider " + getProvider().getProviderName());
		}

		Disk newDisk = new Disk();
		
		if(!isVolumeSizeDeterminedByProduct()){
			newDisk.setSize(options.getVolumeSize().longValue());
		}
		
		if(FCOProviderUtils.isSet(options.getVolumeProductId())){
			String[] productIdSplit = options.getVolumeProductId().split("\\?", 2);
			
			newDisk.setProductOfferUUID(productIdSplit[0]);
			
			if(productIdSplit.length > 1){
				String[] split = productIdSplit[1].split("&");
				
				for(String input : split){
					if(input.startsWith("size")){
						newDisk.setSize(Integer.parseInt(input.split("=")[1].trim()));
					}
				}
			}
		}
		
		newDisk.setResourceName(options.getDescription());
		newDisk.setResourceName(options.getName());

		if(FCOProviderUtils.isSet(options.getVirtualMachineId())){
			newDisk.setServerUUID(options.getVirtualMachineId());
		}
		if(FCOProviderUtils.isSet(options.getDataCenterId())){
			newDisk.setVdcUUID(options.getDataCenterId());
		}else{
			// VDC UUID is mandatory, we need to populate it if it is not supplied. This will get the default vdc for the customer on the cluster.
			newDisk.setVdcUUID(FCOProviderUtils.getDefaultVDC(userService, getProvider().getContext().getRegionId(), getProvider().getContext().getAccountNumber()));
		}

		if(FCOProviderUtils.isSet(options.getSnapshotId())){
			newDisk.setSnapshotUUID(options.getSnapshotId());
		}
		if(FCOProviderUtils.isSet(options.getDeviceId())){
			newDisk.setStorageUnitUUID(options.getDeviceId());
		}

		try{
			Job job = userService.createDisk(newDisk, null);

			userService.waitForJob(job.getResourceUUID(), true);

			return job.getItemUUID();

		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void attach(@Nonnull String volumeId, @Nonnull String toServer, @Nonnull String deviceId) throws InternalException, CloudException {

		try{
			userService.attachDisk(toServer, volumeId, 0, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void detach(@Nonnull String volumeId, boolean force) throws InternalException, CloudException {
		try{

			Disk disk = (Disk) FCOProviderUtils.getResource(userService, volumeId, ResourceType.DISK);

			if(disk == null){
				throw new CloudException("Was unable to find Disk " + volumeId);
			}

			userService.detachDisk(disk.getServerUUID(), volumeId, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public Volume getVolume(@Nonnull String volumeId) throws InternalException, CloudException {
		try{
			return createVolumeFromDisk((Disk) FCOProviderUtils.getResource(userService, volumeId, ResourceType.DISK));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void remove(String volumeId) throws InternalException, CloudException {
		try{
			Job job = userService.deleteResource(volumeId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public int getMaximumVolumeCount() throws InternalException, CloudException {
		return -2;
	}

	@Override
	@Nonnull
	public Requirement getVolumeProductRequirement() throws InternalException, CloudException {
		return Requirement.REQUIRED;
	}

	@Override
	@Nonnull
	public Iterable<VolumeProduct> listVolumeProducts() throws InternalException, CloudException {
		ArrayList<VolumeProduct> list = new ArrayList<VolumeProduct>();

		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("product.associatedType");
		condition.getValue().add(ResourceType.DISK.name());
		filter.getFilterConditions().add(condition);

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.PRODUCTOFFER);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object productOffer : result.getList()){
				list.addAll(createVolumeProductsFromProductOffer((ProductOffer) productOffer, null));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	@Nonnull
	public Iterable<Volume> listVolumes(@Nullable VolumeFilterOptions options) throws InternalException, CloudException {

		ArrayList<Volume> list = new ArrayList<Volume>();

		SearchFilter filter = new SearchFilter();

		if(options.hasCriteria()){

			if(options.getAttachedTo() != null && !options.getAttachedTo().isEmpty()){
				FilterCondition condition = new FilterCondition();
				condition.setCondition(Condition.IS_EQUAL_TO);
				condition.setField("serverUUID");
				condition.getValue().add(options.getAttachedTo());
			}
		}

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.DISK);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				Volume volume = createVolumeFromDisk((Disk) obj);

				if(options.matches(volume)){
					list.add(volume);
				}
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public boolean isVolumeSizeDeterminedByProduct() throws InternalException, CloudException {
		return true;
	}

	@Override
	@Nonnull
	public Iterable<VolumeFormat> listSupportedFormats() throws InternalException, CloudException {
		List<VolumeFormat> formats = new ArrayList<VolumeFormat>();

		formats.add(VolumeFormat.BLOCK);

		return formats;
	}

	@Override
	@Nonnull
	public Iterable<ResourceStatus> listVolumeStatus() throws InternalException, CloudException {
		List<ResourceStatus> list = new ArrayList<ResourceStatus>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.DISK);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				Disk disk = (Disk) obj;
				list.add(new ResourceStatus(disk.getResourceUUID(), getVolumeStateFromResourceState(disk.getResourceState())));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	@Nonnull
	public String[] mapServiceAction(@Nonnull ServiceAction action) {
		return new String[0];
	}

	// --------- Helper Methods ---------- //

	private static List<VolumeProduct> createVolumeProductsFromProductOffer(ProductOffer productOffer, String inputConfiguration) {
		if(productOffer == null){
			return null;
		}

		Set<Integer> diskSizes = new HashSet<Integer>();
		if(FCOProviderUtils.isSet(inputConfiguration)){
			String[] split = inputConfiguration.split("&");

			for(String input : split){
				if(input.startsWith("size")){
					diskSizes.add(Integer.parseInt(input.split("=")[1].trim()));
				}
			}
		}else{
			List<ProductComponent> components = productOffer.getComponentConfig();
			for(ProductComponent component : components){
				for(Value value : component.getProductConfiguredValues()){
					if(value.getName().toLowerCase().contains("disk") && value.getName().toLowerCase().contains("size")){
						if(value.getValue() != null){
							diskSizes.add(Integer.parseInt(value.getValue().trim()));
						}else if(FCOProviderUtils.isSet(value.getValidator().getValidateString())){

							if(value.getValidator().getValidateString().contains(",")){

								String[] options = value.getValidator().getValidateString().split(",");

								for(String option : options){
									if(option == null || option.isEmpty()){
										continue;
									}

									diskSizes.add(Integer.parseInt(option.trim()));
								}

							}else if(value.getValidator().getValidateString().contains("-")){

								String[] minMax = value.getValidator().getValidateString().split("-");

								Integer min = Integer.parseInt(minMax[0].trim());
								Integer max = Integer.parseInt(minMax[1].trim());

								diskSizes.add(max);
								diskSizes.add(min);

								Integer current = max / 2;
								while (current > min){
									diskSizes.add(current);
									current = current / 2;
								}
							}else{
								diskSizes.add(20);
							}

						}else{
							diskSizes.add(20);
						}
					}
				}
			}
		}

		if(diskSizes.size() == 0){
			diskSizes.add(20);
		}

		List<VolumeProduct> list = new ArrayList<VolumeProduct>();

		for(Integer size : diskSizes){
			list.add(VolumeProduct.getInstance(productOffer.getResourceUUID() + "?size=" + size, productOffer.getResourceName(), productOffer.getResourceName(), VolumeType.HDD, Storage.valueOf(size, "gb")));
		}

		return list;
	}

	private static Volume createVolumeFromDisk(Disk disk) {

		if(disk == null){
			return null;
		}

		Volume volume = new Volume();

		volume.setCreationTimestamp(disk.getResourceCreateDate().toGregorianCalendar().getTimeInMillis());
		volume.setCurrentState(getVolumeStateFromResourceState(disk.getResourceState()));
		volume.setDescription(disk.getResourceName());
		volume.setDeviceId(disk.getStorageUnitUUID());
		volume.setName(disk.getResourceName());
		volume.setGuestOperatingSystem(Platform.guess(disk.getImageName()));
		volume.setProviderDataCenterId(disk.getVdcUUID());
		volume.setProviderProductId(disk.getProductOfferUUID() + "?size="+disk.getSize());
		volume.setProviderRegionId(disk.getClusterUUID());
		volume.setProviderSnapshotId(disk.getSnapshotUUID());
		volume.setProviderVirtualMachineId(disk.getServerUUID());
		volume.setProviderVolumeId(disk.getResourceUUID());
		volume.setSize(Storage.valueOf(disk.getSize(), "gigabyte"));
		volume.setType(VolumeType.HDD);
		volume.setFormat(VolumeFormat.BLOCK);

		return volume;
	}

	private static VolumeState getVolumeStateFromResourceState(ResourceState resourceState) {

		switch(resourceState){
		case ACTIVE:
			return VolumeState.AVAILABLE;
		case CREATING:
			return VolumeState.PENDING;
		case DELETED:
		case HIDDEN:
		case LOCKED:
		case TO_BE_DELETED:
		default:
			return VolumeState.DELETED;
		}
	}
}
