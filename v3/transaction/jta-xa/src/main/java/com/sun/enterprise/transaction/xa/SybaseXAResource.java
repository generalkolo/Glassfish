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

//Source File Name:   SybaseXAResource.java

package com.sun.enterprise.transaction.xa;

import javax.transaction.xa.*;
import javax.resource.ResourceException;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * XA Resource wrapper class for sybase XA Resource with jConnect 5.2.
 *
 * @author <a href="mailto:bala.dutt@sun.com">Bala Dutt</a>
 * @version 1.0
 */
public class SybaseXAResource extends XAResourceWrapper
{


	// Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(SybaseXAResource.class);
  /**
   * Returns xids list for recovery depending on flags. Sybase XA Resource ignores the flags
   * for XAResource recover call. This method takes care for the fault. Allows the recover call
   * only for TMSTARTRSCAN, for other values of flags just returns null.
   *
   * @param flag an <code>int</code> value
   * @return a <code>Xid[]</code> value
   * @exception XAException if an error occurs
   */
  public Xid[] recover(int flag) throws XAException {
        try{
            if(flag==XAResource.TMSTARTRSCAN)
                return m_xacon.getXAResource().recover(flag);
        }catch(ResourceException e){
            //a bad xa connection given...
            // throw new XAException("sybase XA resource wrapper : Could not connect : sqlexception was "+e);
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed",e));
        }
        return null;
    }
    public void commit(Xid xid, boolean flag) throws XAException{
        try{
            m_xacon.getXAResource().commit(xid, flag);
        }catch(ResourceException e){
            //a bad xa connection given...
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed",e));
            // throw new XAException("sybase XA resource wrapper :Could not connect : sqlexception was "+e);
        }
    }
    public void rollback(Xid xid) throws XAException{
        try{
            m_xacon.getXAResource().rollback(xid);
        }catch(ResourceException e){
            //a bad xa connection given...
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed",e));
            // throw new XAException("sybase XA resource wrapper :Could not connect : sqlexception was "+e);
        }
    }
}
