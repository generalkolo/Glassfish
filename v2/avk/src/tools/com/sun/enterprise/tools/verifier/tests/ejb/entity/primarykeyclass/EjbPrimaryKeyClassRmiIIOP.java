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
package com.sun.enterprise.tools.verifier.tests.ejb.entity.primarykeyclass;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.tools.verifier.tests.ejb.RmiIIOPUtils;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.*;
import java.lang.ClassLoader;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * Define primary key class which must be legal Value Type in RMI-IIOP test.  
 *
 * Enterprise Bean's primary key class 
 * The Bean provider must specify a primary key class in the deployment 
 * descriptor. The primary key class must be a legal Value Type in RMI-IIOP. 
 *
 */
public class EjbPrimaryKeyClassRmiIIOP extends EjbTest implements EjbCheck { 


    /** 
     * Define primary key class which must be legal Value Type in RMI-IIOP test.  
     *
     * Enterprise Bean's primary key class 
     * The Bean provider must specify a primary key class in the deployment 
     * descriptor. The primary key class must be a legal Value Type in RMI-IIOP. 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    boolean isLegalRMIIIOPValueType = false;
	    boolean oneFailed = false;
  
	    // retrieve the EJB primary key class 
	    String primaryKeyType = ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
  	  
	    if (!primaryKeyType.equals("")) {
		try {
		    Context context = getVerifierContext();
		    ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), false, getVerifierContext().getClassLoader());
		    if (RmiIIOPUtils.isValidRmiIIOPValueType(c)) {
			// this is the right primary key class 
			isLegalRMIIIOPValueType = true;
		    } // return valid
  
		    if (isLegalRMIIIOPValueType) {
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For EJB primary key class [ {0} ]",
					       new Object[] {primaryKeyType}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "A primary key class which must be legal Value Type in RMI-IIOP was defined in the deployment descriptor."));
		    } else if (!isLegalRMIIIOPValueType) {
			oneFailed = true;
			result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".debug1",
						"For EJB primary key class [ {0} ]",
						new Object[] {primaryKeyType}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: A primary key class which must be legal Value Type in RMI-IIOP was not defined in the deployment descriptor."));
		    } 
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: [ {0} ] class not found.",
				   new Object[] {primaryKeyType}));
		    oneFailed = true;
		}
	    } else {
		oneFailed = true;
		result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".debug1",
					"For EJB primary key class [ {0} ]",
					new Object[] {primaryKeyType}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".failed1",
					"Error: A primary key class was not defined in the deployment descriptor."));
	    }

	    if (oneFailed)  {
		result.setStatus(result.FAILED);
	    } else {
		result.setStatus(result.PASSED);
	    }
  
	    return result;

	} else {
	    result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }
}
