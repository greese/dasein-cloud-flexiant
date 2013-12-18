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

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceFeature;

import com.extl.jade.user.UserService;

/**
 * Helper class used when connecting to the FCO User API Service
 * 
 * Used by FCOProviderUtils method getUserServiceFromContext
 * 
 * @version 2013.12 initial version
 * @since 2013.12
 */
public class UserAPIService extends Service {
	
	protected UserAPIService(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}
	
	@WebEndpoint(name = "UserServicePort")
	public UserService getUserServicePort(WebServiceFeature... features) {
		return super.getPort(new QName("http://extility.flexiant.net", "UserServicePort"), UserService.class, features);
	}
}
