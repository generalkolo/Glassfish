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
package com.sun.enterprise.transaction;

import javax.transaction.*;
import javax.transaction.xa.*;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkException;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import com.sun.enterprise.util.i18n.StringManager;

import org.jvnet.hk2.annotations.Service;

/**
 ** Implementation of JavaEETransactionManagerDelegate that supports only
 * local transactions with a single non-XA resource.
 *
 * @author Marina Vatkina
 */
@Service
public class JavaEETransactionManagerSimplifiedDelegate 
            implements JavaEETransactionManagerDelegate {

    private JavaEETransactionManagerSimplified tm;

    // Sting Manager for Localization
    private static StringManager sm
           = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private boolean lao = false;

    public boolean useLAO() {
         return lao;
    }

    public void setUseLAO(boolean b) {
        lao = b;
    }

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void commitDistributedTransaction() throws 
            RollbackException, HeuristicMixedException, 
            HeuristicRollbackException, SecurityException, 
            IllegalStateException, SystemException {} 

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void rollbackDistributedTransaction() throws IllegalStateException, 
            SecurityException, SystemException {} 

    public int getStatus() throws SystemException {
        JavaEETransaction tx = tm.getCurrentTransaction();
        if ( tx != null && tx.isLocalTx())
            return tx.getStatus();
        else
            return javax.transaction.Status.STATUS_NO_TRANSACTION;
    }

    public Transaction getTransaction() 
            throws SystemException {
        return  tm.getCurrentTransaction();
    }

    public boolean enlistDistributedNonXAResource(Transaction tran, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
    }

    public boolean enlistLAOResource(Transaction tran, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {

        return false;
    }

    public void setRollbackOnlyDistributedTransaction()
            throws IllegalStateException, SystemException {
        /** XXX Throw an exception ??? XXX **/
    }

    public Transaction suspend(JavaEETransaction tx) throws SystemException {
        if ( tx != null )
            tm.setCurrentTransaction(null);
        return tx;
    }

    public void resume(Transaction tx)
        throws InvalidTransactionException, IllegalStateException,
        SystemException {
        /** XXX Throw an exception ??? XXX **/
    }

    public void removeTransaction(Transaction tx) {}

    public int getOrder() {
        return 1;
    }

    public void setTransactionManager(JavaEETransactionManager tm) {
        this.tm = (JavaEETransactionManagerSimplified)tm;
    }

    public void startJTSTx(JavaEETransaction t) 
            throws RollbackException, IllegalStateException, SystemException {
        /** XXX Throw an exception ??? XXX **/
    }

    public void recover(XAResource[] resourceList) {
        throw new UnsupportedOperationException("recover");
    }

    public XATerminator getXATerminator() {
        throw new UnsupportedOperationException("getXATerminator");
    }

    public void release(Xid xid) throws WorkException {
        throw new UnsupportedOperationException("release");
    }

    public void recreate(Xid xid, long timeout) throws WorkException {
        throw new UnsupportedOperationException("recreate");
    }
}
