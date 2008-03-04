/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.glassfish.bootstrap.Main;
import com.sun.enterprise.admin.cli.util.CLIUtil;

public class StartDomainCommand extends Command
{
    private static final String GLASSFISH_V3_JAR = "glassfish-10.0-SNAPSHOT.jar";
    private static final String VERBOSE = "verbose";
    private static String PASSWORDFILE = "passwordfile";
    
    
    public boolean validateOptions() throws CommandValidationException
    {
        return true;
    }

    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        final String passwordFile = getOption(PASSWORDFILE);
        Map passwordOptions = null;
        if (passwordFile != null) {
                //currently this is not used
            passwordOptions = CLIUtil.readPasswordFileOptions(passwordFile);
        }
        
        if (getBooleanOption(VERBOSE)) {
             //not sure what arguments are needed
             //for now, just send an empty String array
            (new Main()).run(new String[]{});
        }
        else {
            try {
                new CLIProcessExecutor().execute(startDomainCmd(), false);
                    //exit the daemon thread
                System.exit(0);
            }
            catch (Exception e) {
                throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name}), e);
            }
        }
    }


    /**
     *  defines the command to start the domain
     */
    private String[] startDomainCmd() throws Exception
    {
        final String root = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        return new String [] {
            System.getProperty("java.home")+File.separator+"bin"+File.separator+"java",
            "-jar",
            root+File.separator+"modules"+File.separator+GLASSFISH_V3_JAR
            };
    }
}


