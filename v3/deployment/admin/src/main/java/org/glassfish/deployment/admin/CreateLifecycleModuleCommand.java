/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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

package org.glassfish.deployment.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;

/**
 * Create lifecycle modules.
 *
 */
@Service(name="create-lifecycle-module")
@I18n("create.lifecycle.module")
@Scoped(PerLookup.class)
public class CreateLifecycleModuleCommand implements AdminCommand {

    @Param(primary=true)
    public String name = null;

    @Param(optional=false)
    public String classname = null;

    @Param(optional=true)
    public String classpath = null;

    @Param(optional=true)
    public String loadorder = null;

    @Param(optional=true, defaultValue="false")
    public Boolean failurefatal = false;

    @Param(optional=true, defaultValue="true")
    public Boolean enabled = true;

    @Param(optional=true)
    public String description = null;

    @Param(optional=true)
    public String target = "server";

    @Param(optional=true, separator=':')
    public Properties property = null;

    @Inject
    Deployment deployment;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateLifecycleModuleCommand.class);
   
    public void execute(AdminCommandContext context) {
        
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        if (deployment.isRegistered(name)) {
            report.setMessage("Lifecycle module with name [" + name + 
                "] already exists");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        DeployCommandParameters commandParams = new DeployCommandParameters();
        commandParams.name = name;
        commandParams.enabled = enabled;

        // create a dummy context to hold params and props
        ExtendedDeploymentContext deploymentContext = new DeploymentContextImpl(report, logger, null, commandParams, null);

        Properties appProps = deploymentContext.getAppProps();

        if (property!=null) {
            appProps.putAll(property);
        }

        // set to default "user", deployers can override it
        appProps.setProperty(ServerTags.OBJECT_TYPE, "user");
        appProps.setProperty(ServerTags.CLASS_NAME, classname);
        if (classpath != null) {
            appProps.setProperty(ServerTags.CLASSPATH, classpath);
        }
        if (loadorder != null) {
            appProps.setProperty(ServerTags.LOAD_ORDER, loadorder);
        }
        appProps.setProperty(ServerTags.IS_FAILURE_FATAL, 
            failurefatal.toString());
 
        appProps.setProperty(ServerTags.IS_LIFECYCLE, "true");

        try  {
            deployment.registerAppInDomainXML(null, deploymentContext);
        } catch(Exception e) {
            report.setMessage("Failed to create lifecycle module: " + e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
