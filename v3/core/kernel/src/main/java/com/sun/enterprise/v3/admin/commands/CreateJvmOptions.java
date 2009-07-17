/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.i18n.StringManager;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.UnknownOptionsAreOperands;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Creates given JVM options in server's configuration.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */

@Service(name="create-jvm-options")   //implements the cli command by this "name"
@Scoped(PerLookup.class)            //should be provided "per lookup of this class", not singleton
@I18n("create.jvm.options")
@UnknownOptionsAreOperands()
public final class CreateJvmOptions implements AdminCommand {

    @Param(name="target", optional=true)
    String target;
    
    //Injection of the config beans is not going to work, because it
    //depends what target is being sent on command line -- this is a temporary measure
    @Inject JavaConfig jc;
    
    @Param(name="jvm_option_name", primary=true)
    String optString;
    
    private static final StringManager lsm = StringManager.getManager(ListJvmOptions.class); 
    private static final Logger logger     = Logger.getLogger(CreateJvmOptions.class.getPackage().getName()); // TODO: change later
    public void execute(AdminCommandContext context) {
        //validate the target first
        logfh("Injected JavaConfig: " + jc);
        
        final ActionReport report = context.getActionReport();
        try {
            List<Joe> joes             = Joe.toJoes(optString);
            joes                       = Joe.pruneJoes(jc.getJvmOptions(), joes);
        
            addX(jc, Joe.toStrings(joes));
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage("created " + joes.size() + " option(s)");
        } catch (IllegalArgumentException iae) {
            report.setMessage(iae.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : 
                lsm.getStringWithDefault("create.jvm.options.failed",
                    "Command: create-jvm-options failed", new String[]{e.getMessage()});
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);        
    }

    public CreateJvmOptions() {
        //for debugging purpose, uncomment the line below to see that a new object is constructed every time!
        logfh(this); //unsafe to generally do this, but I am sending it to a private method in a "final" class
    }

    private static void logfh(Object o) {
        if (logger.isLoggable(Level.FINE)) {
            if (o == null) 
                logger.fine("null reference passed");
            else
                logger.fine("Hashcode of the given object: " + o.hashCode());
        }
    }
    
    /** Adds the JVM option transactionally.
     * @throws java.lang.Exception
     */
    // following should work in the fullness of time ...
    /*
    private static void addX(JavaConfig jc, final String option) throws Exception {
        SingleConfigCode<JavaConfig> scc = new SingleConfigCode<JavaConfig> () {
            public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
                List<String> jvmopts = jc.getJvmOptions();
                jvmopts.add(option);
                return ( jc.getJvmOptions() );
            }
        };
        ConfigSupport.apply(scc, jc);
    }
    */
    //@ForTimeBeing :)
    private static void addX(JavaConfig jc, final List <String> newOpts) throws Exception {
        SingleConfigCode<JavaConfig> scc = new SingleConfigCode<JavaConfig> () {
            public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
                List<String> jvmopts = new ArrayList<String>(jc.getJvmOptions()); //copy
                jvmopts.addAll(newOpts);
                jc.setJvmOptions(jvmopts);
                return true;
            }
        };
        ConfigSupport.apply(scc, jc);
    }
}
