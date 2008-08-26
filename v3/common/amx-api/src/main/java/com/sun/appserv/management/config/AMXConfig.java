/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.appserv.management.config;

import com.sun.appserv.management.base.AMX;

import java.util.Map;


/**
	All Config MBeans must extend this interface, whether they
	represent elements or whether they are managers. Extending
	this interface implies that the class is part of the  API
	for configuration.
	<p>
	All AMXConfig s are required to implement NotificationEmitter.
	A Config  must issue {@link javax.management.AttributeChangeNotification} when
	changes are made to the configuration.
 */
public interface AMXConfig extends AMX, AttributeResolver
{
	/**
		The type of the Notification emitted when a config element
		is created.
	 */
	public final String	CONFIG_CREATED_NOTIFICATION_TYPE	=
		"com.sun.appserv.management.config.ConfigCreated";
    
	/**
		The type of the Notification emitted when a config element
		is removed.
	 */
	public final String	CONFIG_REMOVED_NOTIFICATION_TYPE	=
		"com.sun.appserv.management.config.ConfigRemoved";
		
	/**
		The key within the Notification's Map of type
		CONFIG_REMOVED_NOTIFICATION_TYPE which yields the ObjectName
		of the  created or removed config.
	 */
	public final String	CONFIG_OBJECT_NAME_KEY	= "ConfigObjectName";

    /** 
        Get the default value of an Attribute.  Works only for Attributes which have default values,
        and which are part of the configuration.   The name passed is case-insensitive and is matched
        ignoring "-" characters. So the following names are equivalent:
        "JDBCConnectionPool", "JdbcConnectionPool", "jdbc-connection-pool", etc.
        
        @param name  the name of the configuration Attribute
        @return the default value, or null if no default exists
     */
    public String getDefaultValue( final String name );
    
	/**
        Return a Map of default values for the specified child type (Containee) j2eeType.
        The resulting Map is keyed by the XML attribute name, <em>not</em> the AMX Attribute name.
        @since Glassfish V3.
        @param byXMLName whether to key the values by the XML attribute name vs the AMX Attribute name
	 */
    public Map<String,String> getDefaultValues(final boolean useAMXAttributeName);
}
