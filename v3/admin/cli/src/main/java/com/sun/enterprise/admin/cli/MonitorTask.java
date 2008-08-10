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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.*;
import java.util.TimerTask;
import java.util.Timer;
import java.io.File;

import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

public class MonitorTask extends TimerTask
{
    private final static LocalStringsImpl strings = new LocalStringsImpl(MonitorCommand.class);
    String type = null;
    Timer timer = null;
    File fileName = null;
    boolean verbose = false;
    String[] remoteArgs;
    
    public MonitorTask() {}


    public MonitorTask(final Timer timer, final String type, final String[] remoteArgs,
                       final boolean verbose, final File fileName)
    {
        this.timer = timer;
        if (type != null)
            this.type = type;
        this.verbose = verbose;
        this.fileName = fileName;
        this.remoteArgs = remoteArgs;

        //print title
        String title = "";
        if ("servlet".equals(type)) {
            title = String.format("%1$-10s %2$-10s %3$-10s", 
            "ActSess", "SessTtl", "SrvltLdC");
        } else if ("httplistener".equals(type)) {
            title = String.format("%1$-4s %2$-4s %3$-4s %4$-4s", 
            "ec", "mt", "pt", "rc");
        } else if ("jvm".equals(type)) {
            title = String.format("%1$-10s %2$-10s %3$-10s %4$-10s",
            "init", "used", "committed", "max");
        } else if ("webmodule".equals(type)) {
            title = String.format(
            "%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-5s %8$-8s %9$-10s %10$-5s",
            "asc", "ast", "rst", "st", "ajlc", "mjlc", "tjlc", "aslc", "mslc", "tslc");
        }
        CLILogger.getInstance().printMessage(title);
    }

    void cancelMonitorTask()
    {
        timer.cancel();
        final String msg = strings.get("monitorCommand.press_to_quit");
        CLILogger.getInstance().printMessage(msg);
    }

    public void run(){
        try {
            //CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            CLIRemoteCommand rc = new CLIRemoteCommand(remoteArgs);
            rc.runCommand();
            // throw away everything but the main atts
            //Map<String,String> mainAtts = rc.getMainAtts();
            //String cmds = mainAtts.get("children");
            //remoteCommands = cmds.split(";");
        }
        catch(Exception e) {
            CLILogger.getInstance().printError(strings.get("monitorCommand.errorRemote", e.getMessage()));
        }
        finally {
            //CLILogger.getInstance().popAndUnlockLevel();
        }
    }

    public void displayDetails(){
        CLILogger.getInstance().printMessage("These are the details");
    }
    
/*    
    synchronized void writeToFile(final String text)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.append(text);
            out.newLine();
            out.close();
        }
        catch (IOException ioe) {
            final String unableToWriteFile = localStrings.getString("commands.monitor.unable_to_write_to_file", new Object[] {fileName.getName()});
            CLILogger.getInstance().printMessage(unableToWriteFile);
            if (verbose) {
                ioe.printStackTrace();
            }
        }
    }
 */
}
