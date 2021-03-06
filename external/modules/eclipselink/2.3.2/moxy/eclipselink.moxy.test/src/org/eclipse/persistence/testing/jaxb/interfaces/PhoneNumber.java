/*******************************************************************************
* Copyright (c) 2011 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
* which accompanies this distribution.
* The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
* and the Eclipse Distribution License is available at
* http://www.eclipse.org/org/documents/edl-v10.php.
*
* Contributors:
*     bdoughan - July 21/2010 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.testing.jaxb.interfaces;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public interface PhoneNumber {

    @XmlAttribute()
    String getType();
    void setType(String type);

    @XmlValue()
    String getValue();
    void setValue(String value);

}