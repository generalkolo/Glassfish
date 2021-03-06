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
package org.eclipse.persistence.internal.oxm.schema.model;

public class Annotation {
    private java.util.List documentation;
    private java.util.List appInfo;

    public Annotation() {
    }

    public void setDocumentation(java.util.List documentation) {
        this.documentation = documentation;
    }

    public java.util.List getDocumentation() {
        return documentation;
    }

    public void setAppInfo(java.util.List appInfo) {
        this.appInfo = appInfo;
    }

    public java.util.List getAppInfo() {
        return appInfo;
    }
}
