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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.compute.AbstractVMSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VMFilterOptions;
import org.dasein.cloud.compute.VMLaunchOptions;
import org.dasein.cloud.compute.VMLaunchOptions.VolumeAttachment;
import org.dasein.cloud.compute.VMScalingOptions;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.compute.VolumeCreateOptions;
import org.dasein.cloud.flexiant.FCOProvider;
import org.dasein.cloud.flexiant.FCOProviderUtils;
import org.dasein.util.uom.storage.Storage;

import com.extl.jade.user.Condition;
import com.extl.jade.user.Disk;
import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.FilterCondition;
import com.extl.jade.user.Firewall;
import com.extl.jade.user.Ip;
import com.extl.jade.user.Job;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Nic;
import com.extl.jade.user.ProductComponent;
import com.extl.jade.user.ProductOffer;
import com.extl.jade.user.ResourceKey;
import com.extl.jade.user.ResourceKeyType;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.Server;
import com.extl.jade.user.ServerStatus;
import com.extl.jade.user.SshKey;
import com.extl.jade.user.Subnet;
import com.extl.jade.user.UserService;
import com.extl.jade.user.Value;

/**
 * The AbstractVMSupport implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOVirtualMachineSupport extends AbstractVMSupport<FCOProvider> {

	private UserService userService = null;

	public FCOVirtualMachineSupport(FCOProvider provider) {
		super(provider);
		userService = FCOProviderUtils.getUserServiceFromContext(provider.getContext());
	}

	@Override
	public String getProviderTermForServer(Locale locale) {
		return "Server";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public VirtualMachine launch(VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {

		Server server = new Server();

		if(withLaunchOptions == null){
			throw new InternalException("No launch options were supplied");
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getFriendlyName())){
			server.setResourceName(withLaunchOptions.getFriendlyName());
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getDataCenterId())){
			server.setVdcUUID(withLaunchOptions.getDataCenterId());
		}else{
			server.setVdcUUID(FCOProviderUtils.getDefaultVDC(userService, getProvider().getContext().getRegionId(), getProvider().getContext().getAccountNumber()));
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getMachineImageId())){
			server.setImageUUID(withLaunchOptions.getMachineImageId());
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getBootstrapKey())){
			server.setServerKey(withLaunchOptions.getBootstrapKey());
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getVolumes())){
			server.getDisks().addAll(createDisksFromVolumeAttachmentArray(withLaunchOptions.getVolumes()));
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getBootstrapUser())){
			server.setInitialUser(withLaunchOptions.getBootstrapUser());
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getBootstrapPassword())){
			server.setInitialPassword(withLaunchOptions.getBootstrapPassword());
		}

		if(FCOProviderUtils.isSet(withLaunchOptions.getStandardProductId())){

			String[] productIdSplit = withLaunchOptions.getStandardProductId().split("\\?", 2);

			server.setProductOfferUUID(productIdSplit[0]);

			if(productIdSplit.length > 1){
				String[] split = productIdSplit[1].split("&");

				for(String input : split){
					if(input.startsWith("ram")){
						server.setRam(Integer.parseInt(input.split("=")[1].trim()));
					}else if(input.startsWith("cpu")){
						server.setCpu(Integer.parseInt(input.split("=")[1].trim()));
					}
				}
			}
		}

		String networkUUID = withLaunchOptions.getVlanId();

		try{
			Object obj = FCOProviderUtils.getResource(userService, networkUUID, ResourceType.NETWORK, true);
			
			if(obj == null){
				obj = FCOProviderUtils.getResource(userService, networkUUID, ResourceType.SUBNET, true);
				
				if(obj != null){
					networkUUID = ((Subnet)obj).getNetworkUUID();
				}
			}
			
		}catch (ExtilityException e){
		}
		
		if(!FCOProviderUtils.isSet(networkUUID)){
			networkUUID = FCOProviderUtils.getDefaultNetwork(userService, getProvider().getContext().getRegionId(), getProvider().getContext().getAccountNumber());
		}

		Nic newNic = new Nic();
		newNic.setNetworkUUID(networkUUID);
		server.getNics().add(newNic);

		try{

			Job job = userService.createServer(server, null, null);
			userService.waitForJob(job.getResourceUUID(), true);

			String serverUUID = job.getItemUUID();
			
			if(FCOProviderUtils.isSet(withLaunchOptions.getFirewallIds())){

				server = (Server) FCOProviderUtils.getResource(userService, serverUUID, ResourceType.SERVER, true);
				if(server == null){
					return null;
				}
				
				for(Nic nic : server.getNics()){
					for(Ip ipAddresses : nic.getIpAddresses()){
						for(String templateUUID : withLaunchOptions.getFirewallIds()){
							job = userService.applyFirewallTemplate(templateUUID, ipAddresses.getIpAddress(), null);
							userService.waitForJob(job.getResourceUUID(), true);
							
							ResourceKey daseinKey = new ResourceKey();
							daseinKey.setName(FCOProviderUtils.DASEIN_FIREWALL_KEY);
							daseinKey.setType(ResourceKeyType.USER_KEY);
							daseinKey.setValue(serverUUID);
							daseinKey.setWeight(0.0);
							
							userService.modifyKey(templateUUID, daseinKey);
						}
					}
				}
			}

			return createVirtualMachineFromServer(userService, (Server) FCOProviderUtils.getResource(userService, serverUUID, ResourceType.SERVER, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void terminate(String vmId, String explanation) throws InternalException, CloudException {
		try{
			Job job = userService.changeServerStatus(vmId, ServerStatus.STOPPED, true, null, null);
			userService.waitForJob(job.getResourceUUID(), true);
			job = userService.deleteResource(vmId, true, null);
			userService.waitForJob(job.getResourceUUID(), true);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {

		ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();

		try{
			ListResult result = FCOProviderUtils.listWithChildren(userService, null, ResourceType.SERVER);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){
				list.add(createVirtualMachineFromServer(userService, (Server) obj));
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	@Nullable
	public VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {

		if(!FCOProviderUtils.isSet(productId)){
			return null;
		}

		try{
			String[] productIdSplit = productId.split("\\?", 2);

			List<VirtualMachineProduct> products = null;

			ProductOffer productOffer = (ProductOffer) FCOProviderUtils.getResource(userService, productIdSplit[0], ResourceType.PRODUCTOFFER);
			if(productOffer == null){
				return null;
			}

			if(productIdSplit.length > 1){
				products = createVirtualMachineProductFromProductOffer(productOffer, productIdSplit[1]);
			}else{
				products = createVirtualMachineProductFromProductOffer(productOffer, null);
			}

			if(products == null || products.size() == 0){
				return null;
			}else{
				return products.get(0);
			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nullable
	public VirtualMachine getVirtualMachine(@Nonnull String vmId) throws InternalException, CloudException {
		try{
			return createVirtualMachineFromServer(userService, (Server) FCOProviderUtils.getResource(userService, vmId, ResourceType.SERVER, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	@Nonnull
	public Iterable<VirtualMachine> listVirtualMachines(@Nullable VMFilterOptions options) throws InternalException, CloudException {
		return listVirtualMachines();
	}

	@Override
	public void reboot(@Nonnull String vmId) throws CloudException, InternalException {
		try{
			userService.changeServerStatus(vmId, ServerStatus.REBOOTING, true, null, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public boolean supportsPauseUnpause(@Nonnull VirtualMachine vm) {
		return false;
	}

	@Override
	public boolean supportsStartStop(@Nonnull VirtualMachine vm) {
		return true;
	}

	@Override
	public boolean supportsSuspendResume(@Nonnull VirtualMachine vm) {
		return false;
	}

	@Override
	public @Nonnull
	Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {

		List<ResourceStatus> list = new ArrayList<ResourceStatus>();

		try{
			ListResult result = FCOProviderUtils.listAllResources(userService, null, ResourceType.SERVER);

			if(result == null || result.getList() == null){
				return list;
			}

			for(Object obj : result.getList()){

				Server server = (Server) obj;

				list.add(new ResourceStatus(server.getResourceUUID(), vmStateFromServerStatus(server.getStatus())));

			}

		}catch (ExtilityException e){
			throw new CloudException(e);
		}

		return list;
	}

	@Override
	public void start(@Nonnull String vmId) throws InternalException, CloudException {
		try{
			userService.changeServerStatus(vmId, ServerStatus.RUNNING, true, null, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public void stop(@Nonnull String vmId, boolean force) throws InternalException, CloudException {
		try{
			userService.changeServerStatus(vmId, ServerStatus.STOPPED, true, null, null);
		}catch (ExtilityException e){
			throw new CloudException(e);
		}
	}

	@Override
	public Iterable<VirtualMachineProduct> listProducts(Architecture arg0) throws InternalException, CloudException {

		List<VirtualMachineProduct> vmpsList = new ArrayList<VirtualMachineProduct>();

		SearchFilter filter = new SearchFilter();
		FilterCondition condition = new FilterCondition();
		condition.setCondition(Condition.IS_EQUAL_TO);
		condition.setField("productAssociatedType");
		condition.getValue().add(ResourceType.SERVER.name());
		filter.getFilterConditions().add(condition);

		try{
			List<Object> productOffers = FCOProviderUtils.listAllResources(userService, filter, ResourceType.PRODUCTOFFER).getList();

			if(productOffers != null && productOffers.size() > 0){

				for(Object obj : productOffers){
					List<VirtualMachineProduct> vmp = createVirtualMachineProductFromProductOffer((ProductOffer) obj, null);
					if(vmp != null && vmp.size() != 0){
						vmpsList.addAll(vmp);
					}
				}
			}
		}catch (ExtilityException e){
			throw new CloudException(e.getMessage());
		}
		return vmpsList;
	}

	@Override
	public VirtualMachine alterVirtualMachine(String vmId, VMScalingOptions options) throws InternalException, CloudException {

		try{
			Server server = (Server) FCOProviderUtils.getResource(userService, vmId, ResourceType.SERVER, true);

			String productOfferUUID = options.getProviderProductId() == null ? "" : options.getProviderProductId().split("\\?", 2)[0];

			if(productOfferUUID != null && !productOfferUUID.isEmpty() && !productOfferUUID.equals(server.getProductOfferUUID())){
				server.setProductOfferUUID(productOfferUUID);
				VolumeCreateOptions vcos[] = options.getVolumes();
				for(VolumeCreateOptions vco : vcos){
					Disk disk = new Disk();
					disk.setServerUUID(vmId);
					if(vco.getDataCenterId() != null){
						disk.setVdcUUID(vco.getDataCenterId());
					}else{
						disk.setVdcUUID(server.getVdcUUID());
					}
					disk.setResourceName(vco.getName());
					if(vco.getVolumeSize() != null){
						// Its always in Giga Bytes. so no need of any conversion
						disk.setSize(vco.getVolumeSize().getQuantity().longValue());
					}
					disk.setProductOfferUUID(vco.getVolumeProductId());
					Job job = userService.createDisk(disk, null);
					userService.waitForJob(job.getResourceUUID(), true);
				}
			}

			Job job = userService.modifyServer(server, null);
			userService.waitForJob(job.getResourceUUID(), true);

			return createVirtualMachineFromServer(userService, (Server) FCOProviderUtils.getResource(userService, vmId, ResourceType.SERVER, true));
		}catch (ExtilityException e){
			throw new CloudException(e);
		}

	}

	// ------ Helper Methods ---------- //

	private static List<VirtualMachineProduct> createVirtualMachineProductFromProductOffer(ProductOffer productOffer, String inputConfiguration) {
		if(productOffer == null){
			return null;
		}

		Map<String, Integer> inputMap = new HashMap<String, Integer>();
		if(FCOProviderUtils.isSet(inputConfiguration)){
			String[] split = inputConfiguration.split("&");

			for(String input : split){
				if(input.startsWith("ram")){
					inputMap.put("RAM", Integer.parseInt(input.split("=")[1].trim()));
				}else if(input.startsWith("cpu")){
					inputMap.put("CPU", Integer.parseInt(input.split("=")[1].trim()));
				}
			}

		}

		HashSet<Integer> ramOptions = new HashSet<Integer>();
		String ramMeasureType = "MB";
		HashSet<Integer> cpuOptions = new HashSet<Integer>();

		List<ProductComponent> components = productOffer.getComponentConfig();
		for(ProductComponent component : components){
			for(Value value : component.getProductConfiguredValues()){
				if(value.getName().contains("RAM")){

					ramMeasureType = value.getMeasureType() != null ? value.getMeasureType().name() : "MB";
					if(inputMap.containsKey("RAM")){
						ramOptions.add(inputMap.get("RAM"));
					}else if(FCOProviderUtils.isSet(value.getValue())){
						ramOptions.add(Integer.parseInt(value.getValue().trim()));
					}else if(FCOProviderUtils.isSet(value.getValidator().getValidateString())){

						if(value.getValidator().getValidateString().contains(",")){

							String[] options = value.getValidator().getValidateString().split(",");

							for(String option : options){
								if(option == null || option.isEmpty()){
									continue;
								}

								ramOptions.add(Integer.parseInt(option.trim()));
							}

						}else if(value.getValidator().getValidateString().contains("-")){

							String[] minMax = value.getValidator().getValidateString().split("-");

							Integer min = Integer.parseInt(minMax[0].trim());
							Integer max = Integer.parseInt(minMax[1].trim());

							ramOptions.add(max);
							ramOptions.add(min);

							Integer current = max / 2;
							while (current > min){
								ramOptions.add(current);
								current = current / 2;
							}
						}else{
							ramOptions.add(256);
						}

					}else{
						ramOptions.add(256);
					}

				}else if(value.getName().contains("CPU")){

					if(inputMap.containsKey("CPU")){
						cpuOptions.add(inputMap.get("CPU"));
					}else if(FCOProviderUtils.isSet(value.getValue())){
						cpuOptions.add(Integer.parseInt(value.getValue().trim()));
					}else if(FCOProviderUtils.isSet(value.getValidator().getValidateString())){

						if(value.getValidator().getValidateString().contains(",")){

							String[] options = value.getValidator().getValidateString().split(",");

							for(String option : options){
								if(option == null || option.isEmpty()){
									continue;
								}

								cpuOptions.add(Integer.parseInt(option.trim()));
							}

						}else if(value.getValidator().getValidateString().contains("-")){
							String[] minMax = value.getValidator().getValidateString().split("-");

							Integer min = Integer.parseInt(minMax[0].trim());
							Integer max = Integer.parseInt(minMax[1].trim());

							cpuOptions.add(max);
							cpuOptions.add(min);

							Integer current = min + min;
							while (current < max){
								cpuOptions.add(current);
								current = current + min;
							}
						}else{
							cpuOptions.add(1);
						}

					}else{
						cpuOptions.add(1);
					}
				}
			}

		}

		List<VirtualMachineProduct> products = new ArrayList<VirtualMachineProduct>();

		if(ramOptions.size() == 0){
			if(inputMap.containsKey("RAM")){
				ramOptions.add(inputMap.get("RAM"));
			}else{
				ramOptions.add(256);
			}
			ramMeasureType = "MB";
		}
		if(cpuOptions.size() == 0){
			if(inputMap.containsKey("CPU")){
				cpuOptions.add(inputMap.get("CPU"));
			}else{
				cpuOptions.add(1);
			}
		}

		if(ramOptions.size() == 1 && cpuOptions.size() == 1){

			int ram = ramOptions.iterator().next();
			int cpu = cpuOptions.iterator().next();

			VirtualMachineProduct vmProduct = new VirtualMachineProduct();

			String id = productOffer.getResourceUUID() + "?ram=" + ram + "&cpu=" + cpu;

			vmProduct.setDescription(productOffer.getResourceName());
			vmProduct.setName(productOffer.getResourceName());
			vmProduct.setProviderProductId(id);
			vmProduct.setCpuCount(cpu);
			vmProduct.setRamSize(Storage.valueOf(ram, ramMeasureType.toLowerCase()));
			vmProduct.setRootVolumeSize(Storage.valueOf(20, "gb"));
			vmProduct.setStandardHourlyRate(0);

			products.add(vmProduct);
		}else{
			for(Integer ram : ramOptions){
				for(Integer cpu : cpuOptions){

					VirtualMachineProduct vmProduct = new VirtualMachineProduct();

					String id = productOffer.getResourceUUID() + "?ram=" + ram + "&cpu=" + cpu;

					vmProduct.setDescription(productOffer.getResourceName());
					vmProduct.setName(productOffer.getResourceName());
					vmProduct.setProviderProductId(id);
					vmProduct.setCpuCount(cpu);
					vmProduct.setRamSize(Storage.valueOf(ram, ramMeasureType.toLowerCase()));
					vmProduct.setRootVolumeSize(Storage.valueOf(20, "gb"));
					vmProduct.setStandardHourlyRate(0);

					products.add(vmProduct);
				}
			}

		}

		return products;
	}

	private static VirtualMachine createVirtualMachineFromServer(UserService userService, Server server) {

		if(server == null){
			return null;
		}

		VirtualMachine virtualMachine = new VirtualMachine();

		virtualMachine.setArchitecture(Architecture.I32);
		virtualMachine.setClonable(true);
		virtualMachine.setCreationTimestamp(server.getResourceCreateDate().toGregorianCalendar().getTimeInMillis());
		virtualMachine.setCurrentState(vmStateFromServerStatus(server.getStatus()));
		virtualMachine.setDescription(server.getResourceName());
		virtualMachine.setImagable(server.getImagePermission().isCanImage());
		virtualMachine.setName(server.getResourceName());
		virtualMachine.setPlatform(Platform.guess(server.getImageName()));
		virtualMachine.setProductId(server.getProductOfferUUID()+"?ram="+server.getRam()+"&cpu="+server.getCpu());
		virtualMachine.setProviderDataCenterId(server.getVdcUUID());
		virtualMachine.setProviderMachineImageId(server.getImageUUID());
		virtualMachine.setProviderOwnerId(server.getClusterUUID());
		virtualMachine.setProviderRegionId(server.getClusterUUID());
		virtualMachine.setProviderVirtualMachineId(server.getResourceUUID());
		virtualMachine.setRootPassword(server.getInitialPassword());
		virtualMachine.setRootUser(server.getInitialUser());
		
		if(server.getSshkeys().size() > 0){
			String[] sshkeys = new String[server.getServerKey().length()];
			int i = 0;
			for(SshKey key : server.getSshkeys()){
				sshkeys[i++] = key.getResourceUUID();
			}
			virtualMachine.setProviderShellKeyIds(sshkeys);
		}
		if(server.getNics().size() > 0){

			List<String> firewallUUID = new ArrayList<String>();
			for(Nic nic : server.getNics()){
				
				if(!FCOProviderUtils.isSet(virtualMachine.getProviderVlanId())){
					virtualMachine.setProviderVlanId(nic.getNetworkUUID());
				}

				try{
					SearchFilter filter = new SearchFilter();
					FilterCondition condition = new FilterCondition();
					condition.setCondition(Condition.IS_EQUAL_TO);
					condition.setField("ipAddress");

					List<String> ipAddresses = new ArrayList<String>();
					for(Ip ip : nic.getIpAddresses()){
						ipAddresses.add(ip.getIpAddress());
					}
					condition.getValue().addAll(ipAddresses);

					ListResult result = FCOProviderUtils.listAllResources(userService, filter, ResourceType.FIREWALL);

					if(result != null && result.getList() != null){
						for(Object obj : result.getList()){
							Firewall firewall = (Firewall) obj;
							firewallUUID.add(firewall.getTemplateUUID());
						}
					}

				}catch (ExtilityException e){
				}
			}
			virtualMachine.setProviderFirewallIds(firewallUUID.toArray(new String[0]));
		}
		if(server.getResourceKey().size() > 0){
			Map<String, String> keys = new java.util.HashMap<String, String>();
			for(ResourceKey key : server.getResourceKey()){
				keys.put(key.getName(), key.getValue());
			}
			virtualMachine.setTags(keys);
		}

		return virtualMachine;
	}

	private Collection<Disk> createDisksFromVolumeAttachmentArray(VolumeAttachment[] volumes) {

		List<Disk> disks = new ArrayList<Disk>();

		for(VolumeAttachment volume : volumes){

			Disk newDisk = new Disk();

			newDisk.setServerUUID(volume.deviceId);
			if(FCOProviderUtils.isSet(volume.existingVolumeId)){
				newDisk.setResourceUUID(volume.existingVolumeId);
			}else{

				newDisk.setSize(volume.volumeToCreate.getVolumeSize().longValue());
				newDisk.setProductOfferUUID(volume.volumeToCreate.getVolumeProductId());
				newDisk.setResourceName(volume.volumeToCreate.getDescription());
				newDisk.setResourceName(volume.volumeToCreate.getName());

				if(FCOProviderUtils.isSet(volume.volumeToCreate.getVirtualMachineId())){
					newDisk.setServerUUID(volume.volumeToCreate.getVirtualMachineId());
				}
				if(FCOProviderUtils.isSet(volume.volumeToCreate.getDataCenterId())){
					newDisk.setVdcUUID(volume.volumeToCreate.getDataCenterId());
				}else{
					newDisk.setVdcUUID(FCOProviderUtils.getDefaultVDC(userService, getProvider().getContext().getRegionId(), getProvider().getContext().getAccountNumber()));
				}

				if(FCOProviderUtils.isSet(volume.volumeToCreate.getSnapshotId())){
					newDisk.setSnapshotUUID(volume.volumeToCreate.getSnapshotId());
				}
				if(FCOProviderUtils.isSet(volume.volumeToCreate.getDeviceId())){
					newDisk.setStorageUnitUUID(volume.volumeToCreate.getDeviceId());
				}
			}

			disks.add(newDisk);
		}

		return disks;
	}

	private static VmState vmStateFromServerStatus(ServerStatus serverStatus) {

		switch(serverStatus){
		case REBOOTING:
			return VmState.REBOOTING;
		case RUNNING:
			return VmState.RUNNING;
		case STOPPED:
			return VmState.STOPPED;
		case STOPPING:
			return VmState.STOPPING;
		case BUILDING:
		case INSTALLING:
		case MIGRATING:
		case RECOVERY:
		case STARTING:
			return VmState.PENDING;
		case DELETING:
		case ERROR:
		default:
			return VmState.TERMINATED;
		}

	}
}
