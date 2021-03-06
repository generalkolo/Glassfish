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

import javax.transaction.xa.*;
import javax.resource.spi.*;

import com.sun.enterprise.PoolManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.distributedtx.J2EETransaction;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Hashtable;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import com.sun.logging.LogDomains;
import com.sun.jts.CosTransactions.Configuration;

/**
 * @author Tony Ng
 *
 */
public class ConnectorXAResource implements XAResource {

    private Object userHandle;
    private ResourceSpec spec;
    private String poolName;
    private ResourceAllocator alloc;
    private PoolManager poolMgr;
    private ManagedConnection localConnection;
    private ClientSecurityInfo info;
    private ConnectionEventListener listener;
    private ResourceHandle localHandle_;

    private static Hashtable listenerTable = new Hashtable();


    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002

    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);



    public ConnectorXAResource(ResourceHandle handle,
                               ResourceSpec spec,
                               ResourceAllocator alloc,
                               ClientSecurityInfo info ) {

        // initially userHandle is associated with mc
        this.poolMgr = Switch.getSwitch().getPoolManager();
        this.userHandle = null;
        this.spec = spec;
	    this.poolName = spec.getConnectionPoolName();
        this.alloc = alloc;
        this.info = info;
        localConnection = (ManagedConnection) handle.getResource();
        localHandle_ = handle;
    }

    public void setUserHandle(Object userHandle) {
        this.userHandle = userHandle;
    }

    private void handleResourceException(Exception ex)
        throws XAException {
        _logger.log(Level.SEVERE,"poolmgr.system_exception",ex);
        XAException xae = new XAException(ex.toString());
        xae.errorCode = XAException.XAER_RMERR;
        throw xae;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        try {
            ResourceHandle handle = getResourceHandle();
            ManagedConnection mc = (ManagedConnection) handle.getResource();
            mc.getLocalTransaction().commit();
        } catch (Exception ex) {
            handleResourceException(ex);
        }finally{
            resetAssociation();
        }
    }

    public void start(Xid xid, int flags) throws XAException {
        try {
            ResourceHandle handle = getResourceHandle();
            if (!localHandle_.equals(handle)) {
                ManagedConnection mc = (ManagedConnection) handle.getResource();
                mc.associateConnection(userHandle);
                LocalTxConnectionEventListener l =
                        (LocalTxConnectionEventListener) handle.getListener();
                if(_logger.isLoggable(Level.FINE)){
                    _logger.log(Level.FINE, "connection_sharing_start",  userHandle);
                }
                l.associateHandle(userHandle, localHandle_);
            }
        } catch (Exception ex) {
            handleResourceException(ex);
        }
    }

    public void end(Xid xid, int flags) throws XAException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "connection_sharing_end");
        }
        try {
            ResourceHandle handleInTransaction = getResourceHandle();
            if (!localHandle_.equals(handleInTransaction)) {
                LocalTxConnectionEventListener l = (LocalTxConnectionEventListener) handleInTransaction.getListener();

                ResourceHandle handle = l.removeAssociation(userHandle);
                if (handle != null) { // not needed, just to be sure.
                    ManagedConnection associatedConnection = (ManagedConnection) handle.getResource();
                    associatedConnection.associateConnection(userHandle);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "connection_sharing_reset_association",
                                userHandle);
                    }
                }
            }
        } catch (Exception e) {
            handleResourceException(e);
        }
    }
    
    public void forget(Xid xid) throws XAException {
	_logger.fine("Well, forget is called for xid :"+xid);
        // no-op
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }
    
    public boolean isSameRM(XAResource other) throws XAException {
        if (this == other) return true;
        if (other == null) return false;
        if (other instanceof ConnectorXAResource) {
            ConnectorXAResource obj = (ConnectorXAResource) other;
            return (this.spec.equals(obj.spec) &&
                    this.info.equals(obj.info));
        } else {
            return false;
        }
    }        

    public int prepare(Xid xid) throws XAException {
	    return Configuration.LAO_PREPARE_OK;
    }
    
    public Xid[] recover(int flag) throws XAException {
        return new Xid[0];
    }
    
    public void rollback(Xid xid) throws XAException {
        try {
	    ResourceHandle handle = getResourceHandle();
	    ManagedConnection mc = (ManagedConnection) handle.getResource();
            mc.getLocalTransaction().rollback();
        } catch (Exception ex) {
            handleResourceException(ex);
        }finally{
            resetAssociation();
        }
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    public static void freeListener(ManagedConnection mc) {
        listenerTable.remove(mc);
    }

      //Commented from 9.1 as it is not used
    /*public static void addListener(ManagedConnection mc, ConnectionEventListener l) {
        listenerTable.put(mc,l);
    }*/

    private ResourceHandle getResourceHandle() throws PoolingException {
        try {
            ResourceHandle h = null;
            J2EETransactionManager txMgr = Switch.getSwitch().getTransactionManager();
            J2EETransaction j2eetran = (J2EETransaction) txMgr.getTransaction();
            if (j2eetran == null) {      //Only if some thing is wrong with tx manager.
                h = localHandle_;        //Just return the local handle.
            } else {
                h = j2eetran.getNonXAResource();
            }
            if (h.getResourceState().isUnenlisted()) {
                ManagedConnection mc = (ManagedConnection) h.getResource();
                // begin the local transaction if first time
                // this ManagedConnection is used in this JTA transaction
                mc.getLocalTransaction().begin();
            }
            return h;
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "poolmgr.system_exception", ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    private void resetAssociation() throws XAException{
        try {
        ResourceHandle handle = getResourceHandle();

            LocalTxConnectionEventListener l = (LocalTxConnectionEventListener)handle.getListener();
            //Get all associated Handles and reset their ManagedConnection association.
            Map associatedHandles = l.getAssociatedHandles();
            if(associatedHandles != null ){
                Set<Map.Entry> userHandles = associatedHandles.entrySet();
                for(Map.Entry userHandleEntry : userHandles ){
                    ResourceHandle associatedHandle = (ResourceHandle)userHandleEntry.getValue();
                    ManagedConnection associatedConnection = (ManagedConnection)associatedHandle.getResource();
                    associatedConnection.associateConnection(userHandleEntry.getKey());
                    if(_logger.isLoggable(Level.FINE)){
                        _logger.log(Level.FINE, "connection_sharing_reset_association",
                                userHandleEntry.getKey());
                    }
                }
                //all associated handles are mapped back to their actual Managed Connection. Clear the associations.
                associatedHandles.clear();
            }

        } catch (Exception ex) {
            handleResourceException(ex);
        }
    }

}
