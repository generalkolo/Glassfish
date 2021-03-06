/*
 * Copyright (c) OSGi Alliance (2009, 2010). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.service.remoteserviceadmin;

import org.osgi.framework.ServiceReference;

/**
 * An Export Reference associates a service with a local endpoint.
 * 
 * The Export Reference can be used to reference an exported service. When the
 * service is no longer exported, all methods must return <code>null</code>.
 * 
 * @ThreadSafe
 * @version $Revision: 8638 $
 */
public interface ExportReference {
	/**
	 * Return the service being exported.
	 * 
	 * @return The service being exported. Must be <code>null</code> when the
	 *         service is no longer exported.
	 */
	ServiceReference getExportedService();

	/**
	 * Return the Endpoint Description for the local endpoint.
	 * 
	 * @return The Endpoint Description for the local endpoint. Must be
	 *         <code>null</code> when the service is no longer exported.
	 */
	EndpointDescription getExportedEndpoint();
}
