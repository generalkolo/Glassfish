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
* mmacivor - April 25/2008 - 1.0M8 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.testing.jaxb.simpledocument;

import org.eclipse.persistence.testing.jaxb.JAXBTestCases;
import javax.xml.bind.JAXBElement;

/**
 * Tests mapping a simple document containing a single xs:integer element to an Integer object
 * @author mmacivor
 *
 */
public class SimpleDocumentIntegerTestCases extends JAXBTestCases {
		private final static String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/simpledocument/integerroot.xml";

	    public SimpleDocumentIntegerTestCases(String name) throws Exception {
	        super(name);
	        setControlDocument(XML_RESOURCE);        
	        Class[] classes = new Class[1];
	        classes[0] = IntegerObjectFactory.class;
	        setClasses(classes);
	    }

	    protected Object getControlObject() {
	    	JAXBElement value = new IntegerObjectFactory().createIntegerRoot();
	    	value.setValue(new Integer(27));
	    	return value;      
	    }
}
