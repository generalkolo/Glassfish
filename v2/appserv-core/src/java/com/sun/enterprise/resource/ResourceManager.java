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
package com.sun.enterprise.resource;

import javax.transaction.Transaction;

/**
 * Interface definition for the Resource Manager. Depending on the
 * ResourceSpec, PoolManager selects appropriate Resource Manager.
 *
 * @author Binod PG
 */ 
public interface ResourceManager {

    /**
     * Returns the current Transaction, resource should be dealing with.
     *
     * @return An instance of Transaction object.
     * @exception PoolingException If there is any error in getting the 
     * 		  transaction
     */
    public Transaction getTransaction() throws PoolingException;

    /**
     * Get the component involved in invocation. Returns null , if there is
     * no component is involved in the current invocation. 
     * 
     * @return object handle
     */
    public Object getComponent() ;
    
    /**
     * Enlist the Resource handle to the transaction.
     * 
     * @param h Resource to be enlisted.
     * @exception PoolingException If there is any error in enlisting.
     */
    public void enlistResource(ResourceHandle h) throws PoolingException;

    /**
     * Register the resource for a transaction's house keeping activities.
     *
     * @param h Resource to be registered.
     * @exception PoolingException If there is any error in registering.     
     */
    public void registerResource(ResourceHandle handle) throws PoolingException;
    
    /**
     * Set the transaction for rolling back.
     */
    public void rollBackTransaction();
    
    /**
     * Delist the resource from the transaction.
     *
     * @param h Resource to be delisted.
     */
    public void delistResource(ResourceHandle resource, int xaresFlag);
    
    /**
     * Unregister the resource from a transaction's list.
     *
     * @param h Resource to be unregistered.     
     */
    public void unregisterResource(ResourceHandle resource, int xaresFlag);
}
