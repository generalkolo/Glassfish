/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.directcollection.identifiedbyname.withoutgroupingelement;

import java.util.Vector;

import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;
import org.eclipse.persistence.testing.oxm.mappings.directcollection.Employee;

public class DirectCollectionWithoutGroupingElementSingleElementTestCases extends XMLMappingTestCases {

  private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/directcollection/identifiedbyname/withoutgroupingelement/DirectCollectionWithoutGroupingElementSingleElement.xml";
  private final static int CONTROL_ID = 123;
	private final static String CONTROL_RESPONSIBILITY1 = "make the coffee";

  public DirectCollectionWithoutGroupingElementSingleElementTestCases(String name) throws Exception {
    super(name);
    setControlDocument(XML_RESOURCE);
		setProject(new DirectCollectionWithoutGroupingElementIdentifiedByNameProject());
  }

  protected Object getControlObject() {
		Vector responsibilities = new Vector();
		responsibilities.addElement(CONTROL_RESPONSIBILITY1);

    Employee employee = new Employee();
    employee.setID(CONTROL_ID);
		employee.setResponsibilities(responsibilities);
    return employee;
  }

}
