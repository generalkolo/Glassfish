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

package com.sun.gjc.common;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulate the DataSource object details obtained from
 * ManagedConnectionFactory.
 *
 * @version 1.0, 02/07/23
 * @author Binod P.G
 */
public class DataSourceSpec implements java.io.Serializable {

    public static final int USERNAME = 1;
    public static final int PASSWORD = 2;
    public static final int URL = 3;
    public static final int LOGINTIMEOUT = 4;
    public static final int LOGWRITER = 5;
    public static final int DATABASENAME = 6;
    public static final int DATASOURCENAME = 7;
    public static final int DESCRIPTION = 8;
    public static final int NETWORKPROTOCOL = 9;
    public static final int PORTNUMBER = 10;
    public static final int ROLENAME = 11;
    public static final int SERVERNAME = 12;
    public static final int MAXSTATEMENTS = 13;
    public static final int INITIALPOOLSIZE = 14;
    public static final int MINPOOLSIZE = 15;
    public static final int MAXPOOLSIZE = 16;
    public static final int MAXIDLETIME = 17;
    public static final int PROPERTYCYCLE = 18;
    public static final int DRIVERPROPERTIES = 19;
    public static final int CLASSNAME = 20;
    public static final int DELIMITER = 21;

    public static final int XADATASOURCE = 22;
    public static final int DATASOURCE = 23;
    public static final int CONNECTIONPOOLDATASOURCE = 24;

    //GJCINT      
    public static final int CONNECTIONVALIDATIONREQUIRED = 25;
    public static final int VALIDATIONMETHOD = 26;
    public static final int VALIDATIONTABLENAME = 27;

    public static final int TRANSACTIONISOLATION = 28;
    public static final int GUARANTEEISOLATIONLEVEL = 29;

    public static final int STATEMENTTIMEOUT = 30;
    public static final int STATEMENTWRAPPING = 31;

    public static final int JDBC30DATASOURCE = 32;

    private ConcurrentHashMap<Integer,String> details = new ConcurrentHashMap<Integer,String>();

    /**
     * Set the property.
     *
     * @param    property    Property Name to be set.
     * @param    value        Value of property to be set.
     */
    public void setDetail(int property, String value) {
        details.put(property, value);
    }

    /**
     * Get the value of property
     * @param property whose value is required
     * @return value of the property.
     */
    public String getDetail(int property) {
        if (details.containsKey(property)) {
            return details.get(property);
        } else {
            return null;
        }
    }

    /**
     * Checks whether two <code>DataSourceSpec</code> objects
     * are equal or not.
     *
     * @param    obj    Instance of <code>DataSourceSpec</code> object.
     */
    public boolean equals(Object obj) {
        if (obj instanceof DataSourceSpec) {
            return this.details.equals(((DataSourceSpec) obj).details);
        }
        return false;
    }

    /**
     * Retrieves the hashCode of this <code>DataSourceSpec</code> object.
     *
     * @return hashCode of this object.
     */
    public int hashCode() {
        return this.details.hashCode();
    }
}
