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

import java.util.Map;

/**
    Mixin interface indicating a provider of default values.  Except for oddball cases
    (probably none), only {@link Container}s should implement DefaultValues.
 */
public interface DefaultValues
{
	/**
        Return a Map of default values for the specified child type (Containee) j2eeType.
        The resulting Map is keyed by the XML attribute name, <em>not</em> the AMX Attribute name.
        @since Glassfish V3.
        @param j2eeType the J2EEType of the child
        @param byXMLName whether to key the values by the XML attribute name vs the AMX Attribute name
	 */
	public Map<String,String> getDefaultValues( final String jeeType, final boolean useAMXAttributeName);
    
    /** deprecated, use getDefaultValues(j2eeType,true) */
    @Deprecated 
	public Map<String,String> getDefaultValues( final String jeeType);
}
