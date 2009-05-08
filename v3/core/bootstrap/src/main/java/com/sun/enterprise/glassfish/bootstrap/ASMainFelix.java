/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.glassfish.bootstrap;

import org.kohsuke.MetaInfServices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@MetaInfServices(AbstractMain.class)
public class ASMainFelix extends ASMainOSGi {
    private static final String FELIX_HOME = "FELIX_HOME";

    protected String getPreferedCacheDir() {
        return "felix-cache/gf/";
    }

    public String getName() {
        return ASMain.Platform.Felix.toString();
    }

    protected void setFwDir() {
        String fwPath = System.getenv(FELIX_HOME);
        if (fwPath == null) {
            // try system property, which comes from asenv.conf
            fwPath = System.getProperty(FELIX_HOME,
                    new File(glassfishDir, "osgi/felix").getAbsolutePath());
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Felix at " + fwPath);
        }
    }

    @Override
    void setUpCache(File sourceDir, File cacheDir) throws IOException {
        System.setProperty("felix.cache.profiledir", cacheDir.getCanonicalPath());
        super.setUpCache(sourceDir, cacheDir);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        cpb.addJar(new File(fwDir, "bin/felix.jar"));
    }

    protected void launchOSGiFW() throws Exception {
        String sysFileURL = new File(fwDir, "conf/system.properties").toURI().toURL().toString();
        System.setProperty("felix.system.properties", sysFileURL);
        String confFileURL = new File(fwDir, "conf/config.properties").toURI().toURL().toString();
        System.setProperty("felix.config.properties", confFileURL);
        Class mc = launcherCL.loadClass(getFWMainClassName());
        final String[] args = new String[0];
        final Method m = mc.getMethod("main", new Class[]{args.getClass()});
        // Call Felix on a daemon Thread so that the thread created by
        // Felix EventDispatcher also inherits the daemon status.
        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                try {
                    m.invoke(null, new Object[]{args});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                }
            }
        },"OSGi Framework Launcher");

        // The EventDispatcher thread in Felix inherits the daemon status of the thread
        // that starts Felix. So, it is very important to start Felix on a daemon thread.
        // Otherwise, the server process would not exit even when all the server specific
        // non-daemon threads are stopped.
        launcherThread.setDaemon(true);
        launcherThread.start();

        // Wait for framework to be started, otherwise the VM would exit since there is no
        // non-daemon thread started yet. The first non-daemon thread is started
        // when our hk2 osgi-adapter is started.
        launcherThread.join();
        logger.fine("Framework successfully started");
    }

    private String getFWMainClassName() {
        return "org.apache.felix.main.Main";
    }

}
