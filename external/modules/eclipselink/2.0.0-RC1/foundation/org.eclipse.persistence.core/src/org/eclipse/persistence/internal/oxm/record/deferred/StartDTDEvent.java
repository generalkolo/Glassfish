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
package org.eclipse.persistence.internal.oxm.record.deferred;

import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.xml.sax.SAXException;

/**
 * <p><b>Purpose</b>: Class to represent the startDTD event
 * <p><b>Responsibilities</b>:<ul>
 * <li> Execute the startDTD event on the given unmarshalRecord with the specified arguments 
 * </ul>
 */
public class StartDTDEvent extends SAXEvent {
    private String name;
    private String publicId;
    private String systemId;

    public StartDTDEvent(String theName, String thePublicId, String theSystemId) {
        name = theName;
        publicId = thePublicId;
        systemId = theSystemId;
    }

    public void processEvent(UnmarshalRecord unmarshalRecord) throws SAXException {
        unmarshalRecord.startDTD(name, publicId, systemId);
    }
}
