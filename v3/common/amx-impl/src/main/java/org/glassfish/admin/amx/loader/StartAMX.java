/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.amx.loader;

import com.sun.appserv.management.util.jmx.JMXUtil;
import org.glassfish.admin.amx.config.AMXConfigRegistrar;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;


import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;


/**
    Initialize AMX:<br/>
    <ul>
    <li>Start the MBeanServer if not already started/li>
    <li>Start the AMX loader which causes AMX MBeans to be registered</li>
    <li>Start the JMXMP connector</li>
    </ul>
 */
final class StartAMX
{
    protected static void debug( final String s ) { System.out.println(s); }
    
    private static StartAMX INSTANCE;
    
    private volatile ObjectName mAMXLoaderObjectName;
    private final AMXConfigRegistrar mConfigRegistrar;
    private final MBeanServer   mMBeanServer;
    private final J2EELoader  mJ2EELoader;

    
    private static volatile boolean    STARTED = false;
    
    private StartAMX( final MBeanServer mbs, final AMXConfigRegistrar registrar )
    {
        mMBeanServer = mbs;
        mConfigRegistrar= registrar;
        
        mJ2EELoader = new J2EELoader(mbs);
    }
    
        public static synchronized StartAMX
    init(final MBeanServer mbs, final AMXConfigRegistrar registrar)
    {
        INSTANCE = new StartAMX( mbs, registrar );
        return INSTANCE;
    }
    
    // @ return the instance or null if AMX has not yet been started
        public static synchronized StartAMX
    getInstance()
    {
        return INSTANCE;
    }
    
        public static boolean
    isStarted()
    {
        return getInstance() != null;
    }
    
        private synchronized void
    loadMBeans()
    {
        // loads the high-level AMX MBeans, like DomainRoot, QueryMgr, etc
        mAMXLoaderObjectName = LoadAMX.loadAMX( mMBeanServer );
        
        // do this before loading any ConfigBeans so that it will auto-sync
        mJ2EELoader.start();
        
        // load config MBeans
        mConfigRegistrar.getAMXConfigLoader().start( mMBeanServer );
    }
    
    /*
    code moved to org.glassfish.admin.mbeanserver.ConnectorsStarter in common/mbeanserver
    
    public JMXServiceURL    getJMXServiceURL() { return mJMXMPServiceURL; }
    
    // these 3 fields are initialized later
    private volatile JMXConnectorServer mJMXMP = null;
    private volatile JMXServiceURL  mJMXMPServiceURL = null;
    private volatile ObjectName     mJMXMPObjectName = null;
    public static final int JMXMP_PORT = 8888;
    
        public static synchronized void
    startConnectors()
    {
        final int TRY_COUNT = 100;
        
        int port = JMXMP_PORT;
        int tryCount = 0;
        while ( tryCount < TRY_COUNT )
        {
            try
            {
                final JMXConnectorServer cs = getInstance().startJMXMPConnectorServer( port );
                break;
            }
            catch( final java.net.BindException e )
            {
            }
            catch( final Exception e )
            {
                throw new RuntimeException(e);
            }
            
            if ( port < 1000 ) {
                port += 1000;   // in case it's a permissions thing
            }
            else {
                port = port + 1;
            }
        }
    }
    
    
        private synchronized JMXConnectorServer
    startJMXMPConnectorServer( int port)
        throws MalformedURLException, IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        if ( mJMXMPObjectName == null )
        {
            final Map<String,Object> env = new HashMap<String,Object>();
            env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol"); 
            env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());

            final JMXServiceURL      serviceURL = new JMXServiceURL("service:jmx:jmxmp://localhost:" + port );
            final JMXConnectorServer jmxmp = JMXConnectorServerFactory.newJMXConnectorServer( serviceURL, env, mMBeanServer);
            
            ObjectName objectName = JMXUtil.newObjectName( "jmxremote:type=jmx-connector,name=jmxmp,port=" + port);
            objectName = mMBeanServer.registerMBean( jmxmp, objectName).getObjectName();
            
            boolean startedOK    = false;
            try
            {
                // start it only if we can register it successsfully
                jmxmp.start();
                startedOK = true;
            }
            finally
            {
                // we do it this way so that the original exeption will be thrown out
                if ( ! startedOK )
                {
                    try { jmxmp.stop(); } catch( Exception e ) { }
                    try { mMBeanServer.unregisterMBean( objectName ); objectName = null;}  catch( Exception e ) {}
                }
            }
            
            mJMXMPServiceURL = serviceURL;
            mJMXMP           = jmxmp;
            mJMXMPObjectName = objectName;
            System.out.println( "JMXMP connector server URL = " + mJMXMPServiceURL );

            // test
           // JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
           // MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        }
        return mJMXMP;
    }

    */
    
        public static synchronized void
    startAMX()
    {
        if ( ! STARTED )
        {
            //startConnectors();
            getInstance().loadMBeans();
            
            STARTED = true;
            // now starting asynchronously...
        }
    }

    public static synchronized void stopAMX() {
        if (INSTANCE!=null) {
            /*
            try {
                INSTANCE.mJMXMP.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }
}




