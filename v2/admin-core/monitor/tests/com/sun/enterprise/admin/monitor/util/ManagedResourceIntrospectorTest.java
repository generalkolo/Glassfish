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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * ManagedResourceIntrospectorTest.java
 *
 * Created on August 7, 2003, 10:48 AM
 */

package com.sun.enterprise.admin.monitor.util;

import junit.framework.*;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;
import javax.management.*;


/**
 * Unit Test for ManagedResourceIntrospector to test appropriateness of
 * introspection for the purposes of generating an MBeanInfo Object
 * @author  sg112326
 */
public class ManagedResourceIntrospectorTest extends TestCase {
    public void testIntrospection(){
        MBeanInfo info = mri.introspect(stats);
        assertNotNull(info);
        assertEquals(mbean.getClass().getName(), info.getClassName());
    }

    public void testGetParameterInfoWithNull(){
        Class[] paramTypes = new Class[]{};
        MBeanParameterInfo[] info = mri.getParameterInfo(paramTypes);
        assertEquals(paramTypes.length, info.length);  
    }
    
    public void testGetParameterInfo(){
        Class[] paramTypes = new Class[]{java.lang.String.class, java.lang.Integer.class, 
            javax.management.j2ee.statistics.Statistic.class};
        MBeanParameterInfo[] info = mri.getParameterInfo(paramTypes);
        assertEquals(paramTypes.length, info.length);
    }
    
    public void testGetAttributeInfoWithNull(){
        MBeanAttributeInfo[] info = mri.getAttributeInfo(null);
        this.assertNull(info);
    }
    
    public void testGetAttributeInfo(){
        MBeanAttributeInfo[] info = mri.getAttributeInfo(stats);
        assertNotNull(info);
        assertEquals(28, info.length);
    }
    
    public void testCreation(){
        assertNotNull(mbean);
        assertNotNull(mri);
        assertNotNull(stats);
    }

    /** Creates a new instance of ManagedResourceIntrospectorTest */
    public ManagedResourceIntrospectorTest(java.lang.String testName) {
        super(testName);
    }

    DynamicMBean mbean=null;
    ManagedResourceIntrospector mri=null; 
    Stats stats;
    protected void setUp() {
        mbean = new GeneratedMonitoringMBeanImpl();
        mri = new ManagedResourceIntrospector(mbean);
        stats = new S1ASJVMStatsImplMock();
    }
    
    protected void tearDown() {
        
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(ManagedResourceIntrospectorTest.class);
        return suite;
    }
    
    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
}
