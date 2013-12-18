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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.dasein.cloud.AbstractCloud;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.admin.AdminServices;
import org.dasein.cloud.ci.CIServices;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.flexiant.compute.FCOComputeServices;
import org.dasein.cloud.flexiant.identity.FCOIdentityServices;
import org.dasein.cloud.flexiant.network.FCONetworkServices;
import org.dasein.cloud.identity.IdentityServices;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.platform.PlatformServices;
import org.dasein.cloud.storage.StorageServices;

import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.UserService;

/**
 * The CloudProvider implementation for the Dasein FCO implementation
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class FCOProvider extends AbstractCloud {
	static private final Logger logger = getLogger(FCOProvider.class);

	@Nonnull
	static private String getLastItem(@Nonnull String name) {
		int idx = name.lastIndexOf('.');

		if(idx < 0){
			return name;
		}else if(idx == (name.length() - 1)){
			return "";
		}
		return name.substring(idx + 1);
	}

	@Nonnull
	static public Logger getLogger(@Nonnull Class<?> cls) {
		String pkg = getLastItem(cls.getPackage().getName());

		if(pkg.equals("flexiant")){
			pkg = "";
		}else{
			pkg = pkg + ".";
		}
		return Logger.getLogger("dasein.cloud.flexiant.std." + pkg + getLastItem(cls.getName()));
	}

	@Nonnull
	static public Logger getWireLogger(@Nonnull Class<?> cls) {
		return Logger.getLogger("dasein.cloud.flexiant.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName()));
	}

	public FCOProvider() {
	}

	@Override
	@Nonnull
	public String getCloudName() {
		ProviderContext ctx = getContext();
		String name = (ctx == null ? null : ctx.getCloudName());

		return (name == null ? "FCO" : name);
	}

	@Override
	@Nonnull
	public String getProviderName() {
		ProviderContext ctx = getContext();
		String name = (ctx == null ? null : ctx.getProviderName());

		return (name == null ? "FCO" : name);
	}

	// ========== Start of FCO Services ========== //

	@Override
	@Nullable
	public AdminServices getAdminServices() {
		return null;
	}

	@Override
	@Nonnull
	public FCODataCenterServices getDataCenterServices() {
		return new FCODataCenterServices(this);
	}

	@Override
	@Nullable
	public IdentityServices getIdentityServices() {
		return new FCOIdentityServices(this);
	}

	@Override
	public ComputeServices getComputeServices() {
		return new FCOComputeServices(this);
	}

	@Override
	public CIServices getCIServices() {
		return new FCOCIServices(this);
	}

	@Override
	public NetworkServices getNetworkServices() {
		return new FCONetworkServices(this);
	}

	@Override
	public PlatformServices getPlatformServices() {
		return null;
	}

	@Override
	@Nullable
	public synchronized StorageServices getStorageServices() {
		return null;
	}

	// ========== End of FCO Services ========== //

	@Override
	@Nullable
	public String testContext() {
		if(logger.isTraceEnabled()){
			logger.trace("ENTER - " + FCOProvider.class.getName() + ".testContext()");
		}
		try{
			ProviderContext ctx = getContext();

			if(ctx == null){
				System.out.println("No context was provided for testing");
				logger.warn("No context was provided for testing");
				return null;
			}
			try{
				UserService userService = FCOProviderUtils.getUserServiceFromContext(ctx);

				try{
					userService.listResources(null, null, ResourceType.USER);
				}catch (ExtilityException e){
					System.out.println("null");
					return null;
				}

				System.out.println(ctx.getAccountNumber());
				return ctx.getAccountNumber();
			}catch (Throwable t){
				logger.error("Error querying API key: " + t.getMessage());
				t.printStackTrace();
				return null;
			}
		}finally{
			if(logger.isTraceEnabled()){
				logger.trace("EXIT - " + FCOProvider.class.getName() + ".textContext()");
			}
		}
	}
}