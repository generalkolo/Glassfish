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

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;

import com.sun.gjc.util.MethodExecutor;

import javax.resource.ResourceException;

import com.sun.logging.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Utility class, which would create necessary Datasource object according to the
 * specification.
 *
 * @version 1.0, 02/07/23
 * @author Binod P.G
 * @see        com.sun.gjc.common.DataSourceSpec
 * @see        com.sun.gjc.util.MethodExcecutor
 */
public class DataSourceObjectBuilder implements java.io.Serializable {

    private DataSourceSpec spec;

    private Hashtable driverProperties = null;

    private MethodExecutor executor = null;

    private static Logger _logger;

    static {
        _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    }

    private static boolean jdbc40;

    static {
        jdbc40 = detectJDBC40();
    }

    private boolean debug = false;

    private static final StringManager sm = StringManager.getManager(
            DataSourceObjectBuilder.class);

    /**
     * Construct a DataSource Object from the spec.
     *
     * @param    spec    <code> DataSourceSpec </code> object.
     */
    public DataSourceObjectBuilder(DataSourceSpec spec) {
        this.spec = spec;
        executor = new MethodExecutor();
    }

    /**
     * Construct the DataSource Object from the spec.
     *
     * @return Object constructed using the DataSourceSpec.
     * @throws    <code>ResourceException</code> if the class is not found or some issue in executing
     * some method.
     */
    public Object constructDataSourceObject() throws ResourceException {
        driverProperties = parseDriverProperties(spec);
        Object dataSourceObject = getDataSourceObject();
        Method[] methods = dataSourceObject.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            //Check for driver properties first since some jdbc properties
            //may be supported in form of driver properties
            if (driverProperties.containsKey(methodName.toUpperCase())) {
                Vector values = (Vector) driverProperties.get(methodName.toUpperCase());
                executor.runMethod(methods[i], dataSourceObject, values);
            } else if (methodName.equalsIgnoreCase("setUser")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.USERNAME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPassword")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PASSWORD), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setLoginTimeOut")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.LOGINTIMEOUT), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setLogWriter")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.LOGWRITER), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDatabaseName")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DATABASENAME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDataSourceName")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DATASOURCENAME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setDescription")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DESCRIPTION), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setNetworkProtocol")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.NETWORKPROTOCOL), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPortNumber")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PORTNUMBER), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setRoleName")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.ROLENAME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setServerName")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.SERVERNAME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxStatements")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXSTATEMENTS), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setInitialPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.INITIALPOOLSIZE), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMinPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MINPOOLSIZE), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxPoolSize")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXPOOLSIZE), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setMaxIdleTime")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXIDLETIME), methods[i], dataSourceObject);

            } else if (methodName.equalsIgnoreCase("setPropertyCycle")) {
                executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PROPERTYCYCLE), methods[i], dataSourceObject);

            }
        }
        return dataSourceObject;
    }

    /**
     * Get the extra driver properties from the DataSourceSpec object and
     * parse them to a set of methodName and parameters. Prepare a hashtable
     * containing these details and return.
     *
     * @param    spec    <code> DataSourceSpec </code> object.
     * @return Hashtable containing method names and parameters,
     * @throws ResourceException    If delimiter is not provided and property string
     * is not null.
     */
    private Hashtable parseDriverProperties(DataSourceSpec spec) throws ResourceException {
        String delim = spec.getDetail(DataSourceSpec.DELIMITER);

        String prop = spec.getDetail(DataSourceSpec.DRIVERPROPERTIES);
        if (prop == null || prop.trim().equals("")) {
            return new Hashtable();
        } else if (delim == null || delim.equals("")) {
            String msg = sm.getString("dsob.delim_not_specified");
            throw new ResourceException(msg);
        }

        Hashtable properties = new Hashtable();
        delim = delim.trim();
        String sep = delim + delim;
        int sepLen = sep.length();
        String cache = prop;
        Vector methods = new Vector();

        while (cache.indexOf(sep) != -1) {
            int index = cache.indexOf(sep);
            String name = cache.substring(0, index);
            if (!name.trim().equals("")) {
                methods.add(name);
                cache = cache.substring(index + sepLen);
            }
        }

        Enumeration allMethods = methods.elements();
        while (allMethods.hasMoreElements()) {
            String oneMethod = (String) allMethods.nextElement();
            if (!oneMethod.trim().equals("")) {
                String methodName = null;
                Vector parms = new Vector();
                StringTokenizer methodDetails = new StringTokenizer(oneMethod, delim);
                for (int i = 0; methodDetails.hasMoreTokens(); i++) {
                    String token = (String) methodDetails.nextToken();
                    if (i == 0) {
                        methodName = token.toUpperCase();
                    } else {
                        parms.add(token);
                    }
                }
                properties.put(methodName, parms);
            }
        }
        return properties;
    }

    /**
     * Creates a Datasource object according to the spec.
     *
     * @return Initial DataSource Object instance.
     * @throws    <code>ResourceException</code> If class name is wrong or classpath is not set
     * properly.
     */
    private Object getDataSourceObject() throws ResourceException {
        String className = spec.getDetail(DataSourceSpec.CLASSNAME);
        try {
            Class dataSourceClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            Object dataSourceObject = dataSourceClass.newInstance();
            return dataSourceObject;
        } catch (ClassNotFoundException cnfe) {
            _logger.log(Level.SEVERE, "jdbc.exc_cnfe_ds", cnfe); 
            String msg = sm.getString("dsob.class_not_found", className);
            throw new ResourceException(msg);
        } catch (InstantiationException ce) {
            _logger.log(Level.SEVERE, "jdbc.exc_inst", className);
            String msg = sm.getString("dsob.error_instantiating", className);
            throw new ResourceException(msg);
        } catch (IllegalAccessException ce) {
            _logger.log(Level.SEVERE, "jdbc.exc_acc_inst", className);
            String msg = sm.getString("dsob.access_error", className);
            throw new ResourceException(msg);
        }
    }

    public static boolean isJDBC40() {
        return jdbc40;
    }

    /**
     * Check whether the jdbc api version is 4.0 or not.
     *
     * @return boolean
     */
    private static boolean detectJDBC40() {
        boolean jdbc40 = false;
        try {
            Class.forName("java.sql.Wrapper");
            jdbc40 = true;
        } catch (ClassNotFoundException cnfe) {
            _logger.log(Level.FINEST, "could not find Wrapper(available in jdbc-40), jdk supports only jdbc-30");
        }
        return jdbc40;
      }
}
