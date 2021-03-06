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

package com.sun.gjc.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import javax.resource.ResourceException;

import com.sun.logging.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Execute the methods based on the parameters.
 *
 * @version 1.0, 02/07/23
 * @author Binod P.G
 */
public class MethodExecutor implements java.io.Serializable {

    private static Logger _logger;

    static {
        _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    }

    private boolean debug = false;

    private static StringManager sm = StringManager.getManager(
            DataSourceObjectBuilder.class);

    /**
     * Exceute a simple set Method.
     *
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     * @param    value    Value to be set.
     * @param    method    <code>Method</code> object.
     * @param    obj    Object on which the method to be executed.
     */
    public void runJavaBeanMethod(String value, Method method, Object obj) throws ResourceException {
        if (value == null || value.trim().equals("")) {
            return;
        }
        try {
            Class[] parameters = method.getParameterTypes();
            if (parameters.length == 1) {
                Object[] values = new Object[1];
                values[0] = convertType(parameters[0], value);
                method.invoke(obj, values);
            }
        } catch (IllegalAccessException iae) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", iae);
            String msg = sm.getString("me.access_denied", method.getName());
            throw new ResourceException(msg);
        } catch (IllegalArgumentException ie) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ie);
            String msg = sm.getString("me.illegal_args", method.getName());
            throw new ResourceException(msg);
        } catch (InvocationTargetException ite) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ite);
            String msg = sm.getString("me.access_denied", method.getName());
            throw new ResourceException(msg);
        }
    }

    /**
     * Executes the method.
     *
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     * @param    method <code>Method</code> object.
     * @param    obj    Object on which the method to be executed.
     * @param    values    Parameter values for executing the method.
     */
    public void runMethod(Method method, Object obj, Vector values) throws ResourceException {
        try {
            Class[] parameters = method.getParameterTypes();
            if (values.size() != parameters.length) {
                return;
            }
            Object[] actualValues = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                String val = (String) values.get(i);
                if (val.trim().equals("NULL")) {
                    actualValues[i] = null;
                } else {
                    actualValues[i] = convertType(parameters[i], val);
                }
            }
            method.invoke(obj, actualValues);
        } catch (IllegalAccessException iae) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", iae);
            String msg = sm.getString("me.access_denied", method.getName());
            throw new ResourceException(msg);
        } catch (IllegalArgumentException ie) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ie);
            String msg = sm.getString("me.illegal_args", method.getName());
            throw new ResourceException(msg);
        } catch (InvocationTargetException ite) {
            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ite);
            String msg = sm.getString("me.access_denied", method.getName());
            throw new ResourceException(msg);
        }
    }

    /**
     * Converts the type from String to the Class type.
     *
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     * @param    type        Class name to which the conversion is required.
     * @param    parameter    String value to be converted.
     * @return    Converted value.
     */
    private Object convertType(Class type, String parameter) throws ResourceException {
        try {
            String typeName = type.getName();
            if (typeName.equals("java.lang.String") || typeName.equals("java.lang.Object")) {
                return parameter;
            }

            if (typeName.equals("int") || typeName.equals("java.lang.Integer")) {
                return new Integer(parameter);
            }

            if (typeName.equals("short") || typeName.equals("java.lang.Short")) {
                return new Short(parameter);
            }

            if (typeName.equals("byte") || typeName.equals("java.lang.Byte")) {
                return new Byte(parameter);
            }

            if (typeName.equals("long") || typeName.equals("java.lang.Long")) {
                return new Long(parameter);
            }

            if (typeName.equals("float") || typeName.equals("java.lang.Float")) {
                return new Float(parameter);
            }

            if (typeName.equals("double") || typeName.equals("java.lang.Double")) {
                return new Double(parameter);
            }

            if (typeName.equals("java.math.BigDecimal")) {
                return new java.math.BigDecimal(parameter);
            }

            if (typeName.equals("java.math.BigInteger")) {
                return new java.math.BigInteger(parameter);
            }

            if (typeName.equals("boolean") || typeName.equals("java.lang.Boolean")) {
                return Boolean.valueOf(parameter);
            }

            return parameter;
        } catch (NumberFormatException nfe) {
            _logger.log(Level.SEVERE, "jdbc.exc_nfe", parameter);
            String msg = sm.getString("me.invalid_param", parameter);
            throw new ResourceException(msg);
    	}
    }
    
}

