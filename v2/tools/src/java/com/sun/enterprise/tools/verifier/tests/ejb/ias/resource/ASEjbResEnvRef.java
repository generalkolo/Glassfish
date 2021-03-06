package com.sun.enterprise.tools.verifier.tests.ejb.ias.resource;

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

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ResourceEnvRef;
import com.sun.enterprise.tools.common.dd.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.JmsDestinationReferenceDescriptor;

/** ejb [0,n]
 *  resource-env-ref [0,n]
 *      resource-env-ref-name [String]
 *      jndi-name [String]
 *
 * The resource-env-ref holds all the runtime bindings for a resource
 * environment reference
 *
 *
 * @author Irfan Ahmed
 */
public class ASEjbResEnvRef extends EjbTest implements EjbCheck { 

    public Ejb testCase = null;
    public ResourceEnvRef resRefs[];
    public Result result;
    public ComponentNameConstructor compName;
    
    public void getEjb(EjbDescriptor desc, SunEjbJar iasJar)
    {
        testCase = super.getEjb(desc.getName(),iasJar);
        result = getInitializedResult();
        compName = new ComponentNameConstructor(desc);
        resRefs = testCase.getResourceEnvRef();
    }
    
    public Result check(EjbDescriptor descriptor) 
    {
        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        boolean oneFailed = false;
        
        if(ejbJar!=null)
        {
            getEjb(descriptor,ejbJar);
            if(resRefs.length > 0)
            {
                for(int j=0;j<resRefs.length;j++)
                {
                    String refName = resRefs[j].getResourceEnvRefName();
                    try
                    {
                        descriptor.getJmsDestinationReferenceByName(refName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB resource-env-ref] : res-env-ref-name {0} is verified with ejb-jar.xml",
                            new Object[]{refName}));
                    }
                    catch(IllegalArgumentException iaex)
                    {
                        Verifier.debug(iaex);
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-env-ref] : The res-env-ref-name {0} is not defined in ejb-jar.xml for this bean",
                            new Object[]{refName}));
                    }
                }
            }
            else
            {
                 result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                          "{0} Does not define any resource-env-ref Elements",
                          new Object[] {descriptor.getName()}));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
}
