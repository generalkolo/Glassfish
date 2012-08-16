/*******************************************************************************
* Copyright (c) 1998, 2011 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
* which accompanies this distribution.
* The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
* and the Eclipse Distribution License is available at
* http://www.eclipse.org/org/documents/edl-v10.php.
*
* Contributors:
*     bdoughan - Oct 27/2009 - 2.0 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.testing.oxm.mappings.keybased.multipletargets.compositekey.elementkey;

import java.util.ArrayList;

import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCollectionReferenceMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.testing.oxm.mappings.keybased.Address;
import org.eclipse.persistence.testing.oxm.mappings.keybased.Root;
import org.eclipse.persistence.testing.oxm.mappings.keybased.multipletargets.Employee;

public class CompositeElementKeyWithGroupingSingleNodeProject extends Project {

    public CompositeElementKeyWithGroupingSingleNodeProject() {
        addDescriptor(getRootDescriptor());
        addDescriptor(getEmployeeDescriptor());
        addDescriptor(getAddressDescriptor());
    }

    private XMLDescriptor getRootDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Root.class);
        descriptor.setDefaultRootElement("root");
        // create employee mapping
        XMLCompositeObjectMapping empMapping = new XMLCompositeObjectMapping();
        empMapping.setAttributeName("employee");
        empMapping.setXPath("employee");
        empMapping.setReferenceClass(Employee.class);
        descriptor.addMapping(empMapping);
        // create address mapping
        XMLCompositeCollectionMapping addMapping = new XMLCompositeCollectionMapping();
        addMapping.setAttributeName("addresses");
        addMapping.setXPath("address");
        addMapping.setReferenceClass(Address.class);
        descriptor.addMapping(addMapping);
        return descriptor;
    }

    private XMLDescriptor getEmployeeDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Employee.class);
        descriptor.setDefaultRootElement("employee");
        // create id mapping
        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        descriptor.addMapping(idMapping);
        // create name mapping
        XMLDirectMapping nameMapping = new XMLDirectMapping();
        nameMapping.setAttributeName("name");
        nameMapping.setXPath("name/text()");
        descriptor.addMapping(nameMapping);
        // create address mapping
        XMLCollectionReferenceMapping addressMapping = new XMLCollectionReferenceMapping();
        addressMapping.useCollectionClass(ArrayList.class);
        addressMapping.setUsesSingleNode(true);
        addressMapping.setAttributeName("addresses");
        addressMapping.setXPath("grouping-element");
        addressMapping.setReferenceClass(Address.class);
        addressMapping.addSourceToTargetKeyFieldAssociation("address/id-city/text()", "city/text()");
        addressMapping.addSourceToTargetKeyFieldAssociation("address/id-zip/text()", "zip/text()");
        descriptor.addMapping(addressMapping);
        return descriptor;
    }

    private XMLDescriptor getAddressDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Address.class);
        descriptor.addPrimaryKeyFieldName("city/text()");
        descriptor.addPrimaryKeyFieldName("zip/text()");
        descriptor.setDefaultRootElement("address");
        // create id mapping
        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@aid");
        descriptor.addMapping(idMapping);
        // create street mapping
        XMLDirectMapping streetMapping = new XMLDirectMapping();
        streetMapping.setAttributeName("street");
        streetMapping.setXPath("street/text()");
        descriptor.addMapping(streetMapping);
        // create city mapping
        XMLDirectMapping cityMapping = new XMLDirectMapping();
        cityMapping.setAttributeName("city");
        cityMapping.setXPath("city/text()");
        descriptor.addMapping(cityMapping);
        // create country mapping
        XMLDirectMapping countryMapping = new XMLDirectMapping();
        countryMapping.setAttributeName("country");
        countryMapping.setXPath("country/text()");
        descriptor.addMapping(countryMapping);
        // create zip mapping
        XMLDirectMapping zipMapping = new XMLDirectMapping();
        zipMapping.setAttributeName("zip");
        zipMapping.setXPath("zip/text()");
        descriptor.addMapping(zipMapping);
        return descriptor;
    }

}