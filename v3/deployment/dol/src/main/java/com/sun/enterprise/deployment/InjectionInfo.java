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
package com.sun.enterprise.deployment;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class InjectionInfo {

    private List<InjectionCapable> injectionResources; 

    // Name of post-consturct method.  Set to null if there is no post-construct
    // method for this class.
    private String postConstructMethodName;

    // Cached post-construct reflection object method.  Only
    // set at runtime.
    private transient Method postConstructMethod;

    // Name of pre-destroy method.  Set to null if there is no pre-destroy
    // method for this class.
    private String preDestroyMethodName;

    // Cached pre-destroy reflection object method.  Only
    // set at runtime.
    private transient Method preDestroyMethod;

    private String className;

    public InjectionInfo() {
    }

    public InjectionInfo(String cName, String postmName, String premName,
            List<InjectionCapable> resources) {
        className = cName;
        postConstructMethodName = postmName;
        preDestroyMethodName = premName;
        injectionResources = resources;
    }

    public List<InjectionCapable> getInjectionResources() {
        if (injectionResources == null) {
            injectionResources = new LinkedList<InjectionCapable>();
        }
        return injectionResources;
    } 

    public void setInjectionResources(List<InjectionCapable> resources) {
        injectionResources = resources; 
    }

    public String getPostConstructMethodName() {
        return postConstructMethodName;
    }

    public void setPostConstructMethodName(String methodName) {
        postConstructMethodName = methodName;
    }

    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

    public void setPostConstructMethod(Method method) {
        postConstructMethod = method;
    }

    public String getPreDestroyMethodName() {
        return preDestroyMethodName;
    }

    public Method getPreDestroyMethod() {
        return preDestroyMethod;
    }

    public void setPreDestroyMethod(Method method) {
        preDestroyMethod = method;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        className = name;
    }
}
