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

package com.sun.ejb.containers;

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.*;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import com.sun.ejb.*;
import com.sun.ejb.portable.*;
import com.sun.enterprise.*;

import com.sun.enterprise.log.Log;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.logging.*;
import java.util.HashMap;
import com.sun.logging.*;

import java.io.IOException;

import com.sun.ejb.spi.io.IndirectlySerializable;
import com.sun.ejb.spi.io.SerializableObjectFactory;

/**
 * Implementation of the EJBLocalObject interface.
 * This is NOT serializable to prevent local references from leaving
 * the JVM.
 * It is extended by the generated concrete type-specific EJBLocalObject
 * implementation (e.g. Hello_EJBLocalObject).
 *
 * @author Mahesh Kannan
 */
public abstract class EJBLocalObjectImpl
    extends EJBLocalRemoteObject
    implements EJBLocalObject, IndirectlySerializable
{
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(EJBLocalObjectImpl.class);
    private static Class[] NO_PARAMS = new Class[] {};    
    private static Method REMOVE_METHOD = null;

    static {
        try {
            REMOVE_METHOD = 
                EJBLocalObject.class.getMethod("remove", NO_PARAMS);
        } catch ( NoSuchMethodException e ) {
            _logger.log(Level.FINE, "Exception retrieving remove method", e);
        }

    }

    // True this local object instance represents the LocalHome view
    // False if this local object instance represents the LocalBusiness view
    private boolean isLocalHomeView;

    private HashMap<String, Object> clientObjectMap =
        new HashMap<String, Object>();
    /**
     * Get the client object corresponding to an EJBLocalObjectImpl.
     * Users of this class cannot
     * assume they can cast an EJBLocalObjectImpl to the object that
     * the client uses,  and vice-versa.  This is overridden in the 
     * InvocationHandler.  Only applicable for local home view. 
     */
    protected Object getClientObject() {
        return this;
    }
    
    void mapClientObject(String intfClassName, Object obj) {
        clientObjectMap.put(intfClassName, obj);
    }
    
    Object getClientObject(String intfClassName) {
        return clientObjectMap.get(intfClassName);
    }

    void setIsLocalHomeView(boolean flag) {
        isLocalHomeView = flag;
    }

    boolean isLocalHomeView() {
        return isLocalHomeView;
    }

    /**
     * Since EJBLocalObject might be a dynamic proxy, the container can't assume
     * it can cast from EJBLocalObject to EJBLocalObjectImpl.  This convenience
     * method is used to hide the logic behind the translation from an
     * client-side EJBLocalObject to the corresponding EJBLocalObjectImpl.  
     * 
     * In the case of a proxy, the invocation handler is the 
     * EJBLocalObjectImpl.  Otherwise, the argument is returned as is.
     * NOTE : To translate in the other direction, use 
     *   EJBLocalObjectImpl.getEJBLocalObject()
     * 
     */
    static EJBLocalObjectImpl toEJBLocalObjectImpl(EJBLocalObject localObj) {
        EJBLocalObjectImpl localObjectImpl;

        if( localObj instanceof EJBLocalObjectImpl ) {
            localObjectImpl = (EJBLocalObjectImpl) localObj;
        } else {
            localObjectImpl = (EJBLocalObjectImpl) 
                Proxy.getInvocationHandler(localObj);
        } 

        return localObjectImpl;
    }    
    
    public EJBLocalHome getEJBLocalHome() throws EJBException {
        container.authorizeLocalMethod(
            BaseContainer.EJBLocalObject_getEJBLocalHome);
        container.checkExists(this);
        
        return container.getEJBLocalHome();
    }
    
    public void remove() throws RemoveException, EJBException {

        // authorization is performed within container
        
        try {
            container.removeBean(this, REMOVE_METHOD, true);
        }  catch(java.rmi.RemoteException re) {
            // This should never be thrown for local invocations, but it's
            // part of the removeBean signature.  If for some strange
            // reason it happens, convert to EJBException
            EJBException ejbEx =new EJBException("unexpected RemoteException");
            ejbEx.initCause(re);
            throw ejbEx;
        }
    }
    
    public Object getPrimaryKey()
        throws EJBException
    {
        if ( container instanceof EntityContainer ) {
            container.authorizeLocalMethod(
                BaseContainer.EJBLocalObject_getPrimaryKey);
            container.checkExists(this);
            
            return primaryKey;
        }
        else {
            throw new EJBException(localStrings.getLocalString(
            "containers.invalid_operation",
            "Invalid operation for Session EJBs."));
        }
    }
    
    public boolean isIdentical(EJBLocalObject other)
        throws EJBException
    {
        container.authorizeLocalMethod(
            BaseContainer.EJBLocalObject_isIdentical);
        container.checkExists(this);
        
        // For all types of beans (entity, stful/stless session),
        // there is exactly one EJBLocalObject instance per bean identity.
        if ( this == other )
            return true;
        else
            return false;
    }
    
    /**
     * Called from EJBUtils.EJBObjectOutputStream.replaceObject
     */
    public SerializableObjectFactory getSerializableObjectFactory() {
        // Note: for stateful SessionBeans, the EJBLocalObjectImpl contains
        // a pointer to the EJBContext. We should not serialize it here.
        
        return new SerializableLocalObject
            (container.getEjbDescriptor().getUniqueId(), isLocalHomeView,
             primaryKey, getSfsbClientVersion());
    }
    
    private static final class SerializableLocalObject
        implements SerializableObjectFactory
    {
        private long containerId;
        private boolean localHomeView;
        private Object primaryKey;
        private long version;   //Used only for SFSBs
        private transient static Logger _logger;
        
        static {
            _logger=LogDomains.getLogger(LogDomains.EJB_LOGGER);
        }
        
        SerializableLocalObject(long containerId, 
                                boolean localHomeView,
                                Object primaryKey, long version) {
            this.containerId = containerId;
            this.localHomeView = localHomeView;
            this.primaryKey = primaryKey;
            this.version = version;
        }
        
        long getVersion() {
            return version;
        }
        
        public Object createObject()
            throws IOException
        {
            BaseContainer container = (BaseContainer) Switch.getSwitch().
                getContainerFactory().getContainer(containerId);
                
            if( localHomeView ) {
                EJBLocalObjectImpl ejbLocalObjectImpl = 
                    container.getEJBLocalObjectImpl(primaryKey);
                ejbLocalObjectImpl.setSfsbClientVersion(version);
                // Return the client EJBLocalObject. 
                return ejbLocalObjectImpl.getClientObject();
            } else {
                EJBLocalObjectImpl ejbLocalBusinessObjectImpl = 
                    container.getEJBLocalBusinessObjectImpl(primaryKey);
                ejbLocalBusinessObjectImpl.setSfsbClientVersion(version);
                // Return the client EJBLocalObject.  
                return ejbLocalBusinessObjectImpl.getClientObject();
            }
        }

    }

}
