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

package org.glassfish.admingui.util;

import javax.faces.model.SelectItem;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.swing.text.html.Option;


/**
 *
 * @author anilam
 */
public class MiscUtil {

        
    public static SelectItem[] getOptions(String[] values){
        if (values == null){
           SelectItem[] options = (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, 0);
           return options;
        }
        SelectItem[] options =
                (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }
   
     public static Option[] getOptionsArray(String[] values){
        Option[] options =
                (Option []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            Option option = getOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }
     
    public static Option getOption(String value, String label) {
	try {
	    return (Option) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
	} catch (Exception ex) {
	    return null;
	}
    } 
     
    public static SelectItem[] getOptions(String[] values, String[] labels){
        SelectItem[] options =
                (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], labels[i]);
            options[i] = option;
        }
        return options;
    }
    
    public static SelectItem[] getModOptions(String[] values){
        int size = (values == null)? 1 : values.length +1;
        SelectItem[] options =
	    (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, size);
        options[0] = getSunOption("", "");
	for (int i = 0; i < values.length; i++) {
	    SelectItem option = getSunOption(values[i], values[i]);
	    options[i+1] = option;
	}
        return options;
    }
   
    public static SelectItem getSunOption(String value, String label) {
	try {
	    return (SelectItem) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
	} catch (Exception ex) {
	    return null;
	}
    }
    
    /**
     * <p>This utility method can be used to create a ValueExpression and set its value.
     * An example usage might look like this:</p>
     * <code>
     *      ValueExpression ve = MiscUtil.setValueExpression("#{myMap}", new HashMap());
     * </code>
     * @param expression The expression to create. Note that this requires the #{ and } wrappers.
     * @param value  The value to which to set the ValueExpression
     * @return The newly created ValueExpression
     */
    public static ValueExpression setValueExpression(String expression, Object value) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().
            createValueExpression(facesContext.getELContext(), expression, Object.class);
        ve.setValue(facesContext.getELContext(), value);
        
        return ve;
    }

    private static Class	     SUN_OPTION_CLASS = null;
    private static Constructor SUN_OPTION_CONSTRUCTOR = null;

    static {
	try {
	    SUN_OPTION_CLASS =
		Class.forName("com.sun.webui.jsf.model.Option");
	    SUN_OPTION_CONSTRUCTOR = SUN_OPTION_CLASS.
		getConstructor(new Class[] {Object.class, String.class});
	} catch (Exception ex) {
	    // Ignore exception here, NPE will be thrown when attempting to
	    // use SUN_OPTION_CONSTRUCTOR.
	}
    }
        

}
