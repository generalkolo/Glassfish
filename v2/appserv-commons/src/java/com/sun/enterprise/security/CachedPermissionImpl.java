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
package com.sun.enterprise.security;

import java.security.Permission;
import com.sun.enterprise.security.CachedPermission;
import com.sun.enterprise.security.PermissionCache;
/**
 * This class is 
 * @author Ron Monzillo
 */

public class CachedPermissionImpl extends Object implements CachedPermission {

    PermissionCache permissionCache;
    Permission permission;
    boolean granted;
    Epoch epoch;

    public CachedPermissionImpl(PermissionCache c, Permission p) {
	this.permissionCache = c;
	this.permission = p;
	epoch = new Epoch();
    }

    public Permission getPermission() {
	return this.permission;
    }

    public PermissionCache getPermissionCache() {
	return this.permissionCache;
    }

    //synchronization done in PermissionCache
    public boolean checkPermission() {
	boolean granted = false;
	if (permissionCache != null) {
	    granted = permissionCache.checkPermission(this.permission,this.epoch);
	}
	return granted;
    }

    // used to hold last result obtained from cache and cache epoch.
    // epoch is used by PermissionCache to determine when result is out of date.
    class Epoch {

	int epoch;
	boolean granted;

	Epoch() {
	    this.epoch = 0;
	    this.granted = false;
	}
    }

}



