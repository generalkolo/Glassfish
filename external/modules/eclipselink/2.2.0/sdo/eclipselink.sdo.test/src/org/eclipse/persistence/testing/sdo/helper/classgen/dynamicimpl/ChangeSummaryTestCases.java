/*******************************************************************************
* Copyright (c) 1998, 2010 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
* which accompanies this distribution.
* The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
* and the Eclipse Distribution License is available at
* http://www.eclipse.org/org/documents/edl-v10.php.
*
* Contributors:
*     bdoughan - Mar 18/2009 - 2.0 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.testing.sdo.helper.classgen.dynamicimpl;

import java.io.InputStream;

import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class ChangeSummaryTestCases extends SDOTestCase {

    private static String XSD = "org/eclipse/persistence/testing/sdo/helper/classgen/dynamicimpl/ChangeSummary.xsd";

    public ChangeSummaryTestCases(String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();
        InputStream xsdInputStream = Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(XSD);
        this.aHelperContext.getXSDHelper().define(xsdInputStream, null);
    }

    public void testCreateObject() {
        DataObject changeSummaryADO = aHelperContext.getDataFactory().create("http://www.example.com", "ChangeSummaryA");
        ChangeSummaryA changeSummaryA = (ChangeSummaryA) changeSummaryADO;

        assertNotNull(changeSummaryADO.getChangeSummary());
        assertNotNull(changeSummaryA.getChanges());
        assertEquals(changeSummaryADO.getChangeSummary(), changeSummaryA.getChanges());
    }

}
