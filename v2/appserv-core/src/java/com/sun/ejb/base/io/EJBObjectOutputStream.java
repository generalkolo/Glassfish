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

package com.sun.ejb.base.io;

import java.io.*;
import java.security.*;
import java.rmi.*;
import javax.ejb.*;
import javax.naming.*;


import java.util.logging.*;
import com.sun.logging.*;

import com.sun.ejb.base.sfsb.util.SimpleKeyGenerator;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.RemoteBusinessWrapperBase;
import com.sun.ejb.containers.StatefulSessionContainer;

import com.sun.ejb.spi.io.IndirectlySerializable;
import com.sun.ejb.spi.io.SerializableObjectFactory;
import com.sun.ejb.spi.io.NonSerializableObjectHandler;

import com.sun.ejb.EJBUtils;
import com.sun.enterprise.Switch;
import com.sun.enterprise.NamingManager;


import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.TaggedProfile ;

import com.sun.enterprise.iiop.SFSBClientVersionManager;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.util.ORBManager;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

/**
 * A class that is used to passivate SFSB conversational state
 *
 * @author Mahesh Kannan
 */
class EJBObjectOutputStream
    extends java.io.ObjectOutputStream
{

    protected static final Logger _ejbLogger =
       LogDomains.getLogger(LogDomains.EJB_LOGGER);

    protected NonSerializableObjectHandler handler;

    static final int EJBID_OFFSET = 0;
    static final int INSTANCEKEYLEN_OFFSET = 8;
    static final int INSTANCEKEY_OFFSET = 12;

    private static final byte HOME_KEY = (byte)0xff;

    EJBObjectOutputStream(OutputStream out,
            boolean replaceObject,
            NonSerializableObjectHandler handler)
        throws IOException
    {
        super(out);
        if (replaceObject == true) {
           enableReplaceObject(replaceObject);
        }

        this.handler = handler;
    }

    /**
     * This code is needed to serialize non-Serializable objects that
     * can be part of a bean's state. See EJB2.0 section 7.4.1.
     */
    protected Object replaceObject(Object obj)
        throws IOException
    {
	Object result = obj;
	if (obj instanceof IndirectlySerializable) {
	    result = ((IndirectlySerializable) obj).getSerializableObjectFactory();
	} else if (obj instanceof RemoteBusinessWrapperBase) {
            result = getRemoteBusinessObjectFactory
                ((RemoteBusinessWrapperBase)obj);
	} else if (StubAdapter.isStub(obj) && StubAdapter.isLocal(obj)) {
	    org.omg.CORBA.Object target = (org.omg.CORBA.Object) obj;
            // If we're here, it's always for the 2.x RemoteHome view.
            // There is no remote business wrapper class.
	    result = getSerializableEJBReference(target, null);
	} else if ( obj instanceof Serializable ) {
	    result = obj;
	} else if (obj instanceof Context) {
            result = new SerializableJNDIContext((Context) obj);
        } else {
	    if (_ejbLogger.isLoggable(Level.FINE)) {
		_ejbLogger.log(Level.FINE,
		    "EJBObjectInputStream_handling_non_serializable_object",
		    obj.getClass().getName());
	    }
    
	    result = (handler == null)
		? obj
		: handler.handleNonSerializableObject(obj);
	}

	return result;
    }

    private Serializable getRemoteBusinessObjectFactory
        (RemoteBusinessWrapperBase remoteBusinessWrapper) 
        throws IOException {
        // Create a serializable object with the remote delegate and
        // the name of the client wrapper class.
        org.omg.CORBA.Object target = (org.omg.CORBA.Object) 
            remoteBusinessWrapper.getStub();
        return getSerializableEJBReference(target, 
                      remoteBusinessWrapper.getBusinessInterfaceName());
    }

    private Serializable getSerializableEJBReference(org.omg.CORBA.Object obj,
                             String remoteBusinessInterface)
	throws IOException
    {
	Serializable result = (Serializable) obj;
	try {
	  	  
	    IOR ior = ((com.sun.corba.ee.spi.orb.ORB)ORBManager.getORB()).getIOR(obj, false);  
	    java.util.Iterator iter = ior.iterator();

	    byte[] oid = null;
	    if (iter.hasNext()) {
		TaggedProfile profile = (TaggedProfile) iter.next();
		ObjectKey objKey = profile.getObjectKey();
		oid = objKey.getId().getId();
	    }

	    long containerId = -1;
	    int keyLength = -1;
	    if ((oid != null) && (oid.length > INSTANCEKEY_OFFSET)) {
		containerId = Utility.bytesToLong(oid, EJBID_OFFSET);
		//To be really sure that is indeed a ref generated
		//  by our container we do the following checks
		keyLength = Utility.bytesToInt(oid, INSTANCEKEYLEN_OFFSET);
        if (oid.length == keyLength + INSTANCEKEY_OFFSET) {
		    boolean isHomeReference =
			((keyLength == 1) && (oid[INSTANCEKEY_OFFSET] == HOME_KEY));
		    if (isHomeReference) {
			result = new SerializableS1ASEJBHomeReference(containerId);
		    } else {
                SerializableS1ASEJBObjectReference serRef =
                    new SerializableS1ASEJBObjectReference(containerId,
			    oid, keyLength, remoteBusinessInterface);
                result = serRef;
                if (serRef.isHAEnabled()) {
                    SimpleKeyGenerator gen = new SimpleKeyGenerator();
                    Object key = gen.byteArrayToKey(oid, INSTANCEKEY_OFFSET, 20);
                    long version = SFSBClientVersionManager.getClientVersion(
                            containerId, key);
                    serRef.setSFSBClientVersion(key, version);
                }
		    }
		}
	    }
	} catch (Exception ex) {
	    _ejbLogger.log(Level.WARNING, "Exception while getting serializable object", ex);
	    IOException ioEx = new IOException("Exception during extraction of instance key");
	    ioEx.initCause(ex);
	    throw ioEx;
	}
	return result;
    }
}

final class SerializableJNDIContext
    implements SerializableObjectFactory
{
    private String name;
    
    SerializableJNDIContext(Context ctx)
        throws IOException
    {
        try {
            // Serialize state for a jndi context.  The spec only requires
            // support for serializing contexts pointing to java:comp/env
            // or one of its subcontexts.  We also support serializing the
            // references to the the default no-arg InitialContext, as well
            // as references to the the contexts java: and java:comp. All
            // other contexts will either not serialize correctly or will
            // throw an exception during deserialization.
            this.name = ctx.getNameInNamespace();
        } catch (NamingException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    public Object createObject()
        throws IOException
    {
        try {
            if ((name == null) || (name.length() == 0)) {
                return new InitialContext();
            } else {
                NamingManager nm = Switch.getSwitch().getNamingManager();
                return nm.restoreJavaCompEnvContext(name);
            }
        } catch (NamingException namEx) {
            IOException ioe = new IOException();
            ioe.initCause(namEx);
            throw ioe;
	}
    }

}

abstract class AbstractSerializableS1ASEJBReference
    implements SerializableObjectFactory
{
    protected long containerId;
    protected String debugStr;	//used for loggin purpose only

    
    protected static Logger _ejbLogger =
       LogDomains.getLogger(LogDomains.EJB_LOGGER);

    AbstractSerializableS1ASEJBReference(long containerId) {
	this.containerId = containerId;
	BaseContainer container = (BaseContainer) Switch.getSwitch().
	    getContainerFactory().getContainer(containerId);
    
	//container can be null if the app has been undeployed
	//  after this was serialized
	if (container == null) {
	    _ejbLogger.log(Level.WARNING, "ejb.base.io.EJBOutputStream.null_container: "
		+ containerId);
	    debugStr = "" + containerId;
	} else {
	    debugStr = container.toString();
	}
    }


    protected static java.rmi.Remote doRemoteRefClassLoaderConversion
        (java.rmi.Remote reference) throws IOException {

        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader =
            currentThread.getContextClassLoader();
        
        java.rmi.Remote returnReference = reference;

        if( reference.getClass().getClassLoader() !=
            contextClassLoader) {
            try {
                byte[] serializedRef = IOUtils.serializeObject
                    (reference, false);
                returnReference = (java.rmi.Remote)
                    IOUtils.deserializeObject(serializedRef, false,
                                              contextClassLoader);
                StubAdapter.connect
                    (returnReference, 
                     (com.sun.corba.ee.spi.orb.ORB) ORBManager.getORB());
            } catch(IOException ioe) {
                throw ioe;
            } catch(Exception e) {
                IOException ioEx = new IOException(e.getMessage());
                ioEx.initCause(e);
                throw ioEx;
            }
        }

        return returnReference;
    }
}

final class SerializableS1ASEJBHomeReference
    extends AbstractSerializableS1ASEJBReference
{
    
    SerializableS1ASEJBHomeReference(long containerId) {
	super(containerId);
    }

    public Object createObject()
        throws IOException
    {
	Object result = null;
	BaseContainer container = (BaseContainer) Switch.getSwitch().
	    getContainerFactory().getContainer(containerId);
	//container can be null if the app has been undeployed
	//  after this was serialized
	if (container == null) {
	    _ejbLogger.log(Level.WARNING, "ejb.base.io.EJBOutputStream.null_container "
		+ debugStr);
	    result = null;
	} else {
            // Note that we can assume it's a RemoteHome stub because an
            // application never sees a reference to the internal 
            // Home for the Remote Business view.
	    result = AbstractSerializableS1ASEJBReference.
                doRemoteRefClassLoaderConversion(container.getEJBHomeStub());
	}

	return result;
    }
}

final class SerializableS1ASEJBObjectReference
    extends AbstractSerializableS1ASEJBReference
{
    private byte[] instanceKey;
    private Object sfsbKey;
    protected long sfsbClientVersion;
    protected boolean haEnabled;

    // If 3.0 Remote business view, the name of the remote business
    // interface to which this stub corresponds.
    private String remoteBusinessInterface;

    SerializableS1ASEJBObjectReference(long containerId, byte[] objKey,
            int keySize, String remoteBusinessInterfaceName) {
        super(containerId);
        BaseContainer container = (BaseContainer) Switch.getSwitch()
                .getContainerFactory().getContainer(containerId);
        if (container != null) {
            this.haEnabled = container.isHAEnabled();
        }
        remoteBusinessInterface = remoteBusinessInterfaceName;
        instanceKey = new byte[keySize];
        System.arraycopy(objKey, EJBObjectOutputStream.INSTANCEKEY_OFFSET,
                instanceKey, 0, keySize);
    }
    
    void setSFSBClientVersion(Object key, long val) {
        this.sfsbKey = key;
        this.sfsbClientVersion = val;
    }
    
    boolean isHAEnabled() {
        return haEnabled;
    }
    
    public Object createObject()
        throws IOException
    {
	Object result = null;
	BaseContainer container = (BaseContainer) Switch.getSwitch().
	    getContainerFactory().getContainer(containerId);
	//container can be null if the app has been undeployed
	//  after this was serialized
	if (container == null) {
	    _ejbLogger.log(Level.WARNING, 
                           "ejb.base.io.EJBOutputStream.null_container: "
                           + debugStr);
	    result = null;
	} else {
            try {
                if( remoteBusinessInterface == null ) {
                    java.rmi.Remote reference = container.
                        createRemoteReferenceWithId(instanceKey, null);
                    result = AbstractSerializableS1ASEJBReference.
                        doRemoteRefClassLoaderConversion(reference);
                        
                } else {

                    String generatedRemoteIntfName = EJBUtils.
                        getGeneratedRemoteIntfName(remoteBusinessInterface);

                    java.rmi.Remote remoteRef = container.
                        createRemoteReferenceWithId(instanceKey, 
                                                    generatedRemoteIntfName);

                    java.rmi.Remote newRemoteRef = 
                        AbstractSerializableS1ASEJBReference.
                            doRemoteRefClassLoaderConversion(remoteRef);

                    
                    Thread currentThread = Thread.currentThread();
                    ClassLoader contextClassLoader =
                        currentThread.getContextClassLoader();   

                    result = EJBUtils.createRemoteBusinessObject
                        (contextClassLoader, remoteBusinessInterface, 
                         newRemoteRef);
                         
                }
                
                if (haEnabled) {
                    SFSBClientVersionManager.setClientVersion(
                            containerId, sfsbKey, sfsbClientVersion);
                }
            } catch(Exception e) {
                IOException ioex = new IOException("remote ref create error");
                ioex.initCause(e);
                throw ioex;
            }
	}

	return result;
    }
}


