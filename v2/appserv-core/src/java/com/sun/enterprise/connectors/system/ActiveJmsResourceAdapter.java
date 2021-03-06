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

package com.sun.enterprise.connectors.system;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;

import com.sun.enterprise.connectors.*;
import com.sun.enterprise.connectors.inflow.*;
import com.sun.enterprise.connectors.util.JmsRaUtil; 
import com.sun.enterprise.connectors.util.SetMethodAction; 
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.connectors.util.ResourcesUtil;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.JmsAvailability;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.jms.IASJmsUtil;
import com.sun.enterprise.jms.JmsProviderLifecycle;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;

/** 
 * Represents an active JMS resource adapter. This does 
 * additional configuration to ManagedConnectionFactory
 * and ResourceAdapter java beans.
 * 
 * XXX: For code management reasons, think about splitting this
 * to a preHawk and postHawk RA (with postHawk RA extending preHawk RA).  
 * 
 * @author Binod P.G, Sivakumar Thyagarajan
 */
public class ActiveJmsResourceAdapter extends ActiveInboundResourceAdapter {

    static Logger logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    private final String SETTER = "setProperty";
    private static final String SEPARATOR = "#";    
    private static final String MQ_PASS_FILE_PREFIX = "asmq";
    private static final String MQ_PASS_FILE_KEY = "imq.imqcmd.password=";

    //RA Javabean properties.
    public static final String CONNECTION_URL = "ConnectionURL";
    private final String RECONNECTENABLED = "ReconnectEnabled";
    private final String RECONNECTINTERVAL = "ReconnectInterval";
    private final String RECONNECTATTEMPTS = "ReconnectAttempts";
    private static final String GROUPNAME = "GroupName";
    private static final String CLUSTERCONTAINER = "InClusteredContainer";
    
    //Lifecycle RA JavaBean properties
    public static final String BROKERTYPE="BrokerType";
    private static final String BROKERINSTANCENAME="BrokerInstanceName";
    private static final String BROKERBINDADDRESS="BrokerBindAddress";
    private static final String BROKERPORT="BrokerPort";
    private static final String BROKERARGS="BrokerArgs";
    private static final String BROKERHOMEDIR="BrokerHomeDir";
    private static final String BROKERLIBDIR ="BrokerLibDir";
    private static final String BROKERVARDIR="BrokerVarDir";
    private static final String BROKERJAVADIR="BrokerJavaDir";
    private static final String BROKERSTARTTIMEOUT="BrokerStartTimeOut";
    private static final String ADMINUSERNAME="AdminUserName";
    private static final String ADMINPASSWORD="AdminPassword";
    private static final String ADMINPASSFILE="AdminPassFile";
    private static final String USERNAME="UserName";
    private static final String PASSWORD="Password";

    private static final String MASTERBROKER="MasterBroker";
    
    //JMX properties
    private static final String JMXSERVICEURL="JMXServiceURL";
    private static final String JMXSERVICEURLLIST="JMXServiceURLList";
    private static final String JMXCONNECTORENV="JMXConnectorEnv";
    private static final String USEJNDIRMISERVICEURL="useJNDIRMIServiceURL";
    private static final String RMIREGISTRYPORT="RmiRegistryPort";
    private static final String USEEXTERNALRMIREGISTRY="startRMIRegistry";
    private static final int DEFAULTRMIREGISTRYPORT =7776;
    private static final int BROKERRMIPORTOFFSET=100;
 
	
    private static final String SSLJMXCONNECTOR="SslJMXConnector";
    
    //Availability properties
    private static final String HAREQUIRED = "HARequired";
    private static final String CLUSTERID = "ClusterId";
    private static final String BROKERID = "BrokerId";
    private static final String PINGINTERVAL = "PingInterval";
    private static final String DBTYPE = "DBType";
    private static final String DBTYPE_HADB="hadb";
    private static final String BROKERENABLEHA = "BrokerEnableHA";
    
    private static final String DB_HADB_PROPS = "DBProps";
    //properties within DB_PROPS
    private static final String DB_HADB_USER = "hadb.user";
    private static final String DB_HADB_PASSWORD = "hadb.password";
    private static final String DB_HADB_DRIVERCLASS = "hadb.driverClass";
    private static final String DS_HADB_PROPS = "DSProps";
    //properties within DS_PROPS
    private static final String DS_HADB_SERVERLIST = "hadb.serverList";
    
    //Not used now.
    private final String CONTAINER = "InAppClientContainer";

    //Activation config properties of MQ resource adapter.
    public static final String DESTINATION        = "Destination";
    public static final String DESTINATION_TYPE   = "DestinationType";
    private static String SUBSCRIPTION_NAME  = "SubscriptionName";
    private static String CLIENT_ID          = "ClientID";
    public static final String PHYSICAL_DESTINATION  = "Name";
    private static String MAXPOOLSIZE  = "EndpointPoolMaxSize";
    private static String MINPOOLSIZE  = "EndpointPoolSteadySize";
    private static String RESIZECOUNT  = "EndpointPoolResizeCount";
    private static String RESIZETIMEOUT  = "EndpointPoolResizeTimeout";
    private static String REDELIVERYCOUNT  = "EndpointExceptionRedeliveryAttempts";
    public static final String ADDRESSLIST  = "AddressList";
    private static String ADRLIST_BEHAVIOUR  = "AddressListBehavior";
    private static String ADRLIST_ITERATIONS  = "AddressListIterations";
    private static final String MDBIDENTIFIER = "MdbName";

    //MCF properties
    private static final String MCFADDRESSLIST = "MessageServiceAddressList";

    private StringManager sm = 
        StringManager.getManager(ActiveJmsResourceAdapter.class);

    private MQAddressList urlList = null;
   
    private String addressList;
    
    private String brkrPort;

    //Properties in domain.xml for HADB JDBC connection pool (for HA)
    private static final String DUSERNAME = "User";
    private static final String DPASSWORD = "Password";
    private static final String DSERVERLIST = "ServerList";
    private static final String HADB_CONNECTION_URL_PREFIX = "jdbc:sun:hadb:";
    
    //Lifecycle properties
    public static final String EMBEDDED="EMBEDDED";
    public static final String LOCAL="LOCAL";
    public static final String REMOTE="REMOTE";
    public static final String DIRECT="DIRECT";

    private final String DEFAULT_STORE_POOL_JNDI_NAME = "jdbc/hastore";

    // Both the properties below are hacks. These will be changed later on.
    private static String MQRmiPort = 
        System.getProperty("com.sun.enterprise.connectors.system.MQRmiPort");
    private static final String DASRMIPORT = "31099";

    private static final String REVERT_TO_EMBEDDED_PROPERTY =
    	"com.sun.enterprise.connectors.system.RevertToEmbedded";	
    private static final String BROKER_RMI_PORT =
    	"com.sun.enterprise.connectors.system.mq.rmiport";	

    private Properties dbProps = null;
    private Properties dsProps = null;
    private String brokerInstanceName = null;
        
    private File mqPassFile = null;
        
    /**
     * Constructor for an active Jms Adapter.
     * 
     * @param ra ResourceAdapter Javabean.
     * @param desc Deployment descriptor object
     * @param moduleName Name of the resource adapter.
     * @parm jcl Class Loader.
     * @throw ConnectorRuntimeException in case of an exception.
     */
    public ActiveJmsResourceAdapter(
            ResourceAdapter ra, ConnectorDescriptor desc, String moduleName, 
            ClassLoader jcl) throws ConnectorRuntimeException {
        super(ra,desc,moduleName,jcl);
        
        //Now that the RA has been started, delete the temp passfile
        if (mqPassFile != null) {
            mqPassFile.delete();
        }
    }

    /**
     * Loads RA configuration for MQ Resource adapter.
     * 
     * @throw ConnectorRuntimeException in case of an exception.
     */
    protected void loadRAConfiguration() throws ConnectorRuntimeException{
        if (ConnectorRuntime.getRuntime().getEnviron() 
                                       == ConnectorRuntime.SERVER) {
            // Check whether MQ has started up or not.
            try {
                if (!JmsProviderLifecycle.shouldUseMQRAForLifecycleControl()) {
                    JmsProviderLifecycle.checkProviderStartup();
                } else {
                	setLifecycleProperties();
                }
    	    } catch (Exception e) {
                ConnectorRuntimeException cre = new ConnectorRuntimeException
                                                        (e.getMessage());
    	        throw (ConnectorRuntimeException) cre.initCause(e);
    	    }
            
            setMdbContainerProperties();
            setJmsServiceProperties(null);
            setClusterRABeanProperties();
            setAvailabilityProperties();
        } else {
            setAppClientRABeanProperties();
        }
        super.loadRAConfiguration();
        postRAConfiguration();
    }

    /*
     * Set Availability related properties
     * If EE: If JMS availability true set availability properties
     * If shared hadb : get HADB CCP information and set accordingly
     * If not shared : read configured pool information and set.
     */
    private void setAvailabilityProperties() throws ConnectorRuntimeException {
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().getConfigContext();
            AvailabilityService as = ServerBeansFactory.getConfigBean(ctx).getAvailabilityService();
            if (as == null) {
                logFine("Availability Service is null. Not setting HA attributes");
                return;
            }
            
            //Only if JMS availability is true
            if (isJMSAvailabilityOn(as)) {
                ConnectorDescriptor cd = getDescriptor();
                //Set HARequired as true - irrespective of whether it is REMOTE or
                //LOCAL
                EnvironmentProperty envProp1 = new EnvironmentProperty (
                                            HAREQUIRED , "true","HA Required", 
                                           "java.lang.String");
                setProperty(cd, envProp1);
                
                JmsService jmsService = ServerBeansFactory.getConfigBean(ctx).
                                                                getJmsService();
                if (isClustered()) {
                    if (jmsService.getType().equals(REMOTE)) {
                        //If REMOTE, the broker cluster instances already have 
                        //been configured with the right properties.
                        return;
                    } else {
                        //LOCAL/EMBEDDED instances in a cluster.
                        String clusterName = getMQClusterName();
                        EnvironmentProperty envProp2 = new EnvironmentProperty (
                                    CLUSTERID , clusterName,"Cluster Id", 
                                    "java.lang.String");
                        setProperty(cd, envProp2);
                        
                        if(brokerInstanceName == null) {
                            brokerInstanceName = getBrokerInstanceName(jmsService); 
                        }
                        EnvironmentProperty envProp3 = new EnvironmentProperty (
                                    BROKERID , brokerInstanceName,"Broker Id", 
                                    "java.lang.String");
                        setProperty(cd, envProp3);
                        
                        
                        EnvironmentProperty envProp4 = new EnvironmentProperty (
                                        DBTYPE , DBTYPE_HADB,"DBType", 
                                        "java.lang.String");
                        setProperty(cd, envProp4);

                        /*
                         * The broker has a property to control whether 
                         * it starts in HA mode or not and that's represented on 
                         * the RA by BrokerEnableHA.
                         * On the MQ Client connection side it is HARequired - 
                         * this does not control the broker, it just is a client 
                         * side requirement.
                         * So for AS EE, if BrokerType is LOCAL or EMBEDDED, 
                         * and AS HA is enabled for JMS then both these must be 
                         * set to true.
                         */
                        EnvironmentProperty envProp5 = new EnvironmentProperty (
                                        BROKERENABLEHA , "true",
                                        "BrokerEnableHA flag","java.lang.Boolean");
                        setProperty(cd, envProp5);
                        
                        //get pool name
                        String poolJNDIName = as.getJmsAvailability().getMqStorePoolName();
                        //If no MQ store pool name is specified, use default poolname
                        //XXX: default pool name is jdbc/hastore but asadmin
                        //configure-ha-cluster creates a resource called 
                        //"jdbc/<asclustername>-hastore" which needs to be used.
                        if (poolJNDIName == null || poolJNDIName =="" ) {
                            //get Web container's HA store's pool name
                            poolJNDIName = as.getWebContainerAvailability().
                                                getHttpSessionStorePoolName();
                            logFine("HTTP Session store pool jndi name " +
                                    "is " + poolJNDIName);
                        }
                        //XXX: request HADB team mq-store-pool name to be 
                        //populated as part of configure-ha-cluster

                        JdbcConnectionPool jdbcConPool = getJDBCConnectionPoolInfo(
                                                            poolJNDIName);
                        //DBProps: compute values from pool object
                        String userName = getPropertyFromPool(jdbcConPool, DUSERNAME);
                        logFine("HA username is " + userName);
                        
                        String password = getPropertyFromPool(jdbcConPool, DPASSWORD);
                        logFine("HA Password is " + password);
                        
                        String driverClass = jdbcConPool.getDatasourceClassname();
                        logFine("HA driverclass" + driverClass);
                        
                        dbProps = new Properties();
                        dbProps.setProperty(DB_HADB_USER, userName);
                        dbProps.setProperty(DB_HADB_PASSWORD, password);
                        dbProps.setProperty(DB_HADB_DRIVERCLASS, driverClass);
                        
                        //DSProps: compute values from pool object
                        String serverList = getPropertyFromPool(jdbcConPool, DSERVERLIST);
                        logFine("HADB server list is " + serverList);
                        dsProps = new Properties();

                        if (serverList != null) {
                            dsProps.setProperty(DS_HADB_SERVERLIST, serverList);
                        } else {
                            logger.warning("ajra.incorrect_hadb_server_list");
                        }
                        
                        //set all other properties in dsProps as well.
                        Properties p = getDSPropertiesFromThePool(jdbcConPool);
                        Iterator iterator = p.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = (String) iterator.next();
                            String val = (String)p.get(key);
                            dsProps.setProperty(key, val);
                        }
                    }
                } else {
                    //ignore. Not clustered.
                    logFine("Instance not clustered. Not setting HA " +
                    "attributes");
                }
            }
        } catch (ConfigException e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(
                            e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }
    
    /*
     * Gets all the other [apart from serverlist] DataSource properties from 
     * the HADB JDBC connection pool.
     */
    private Properties getDSPropertiesFromThePool(JdbcConnectionPool jdbcConPool) {
        Properties p = new Properties();
        ElementProperty[] elemProp = jdbcConPool.getElementProperty();
        Set<String> excludeList = new HashSet<String>();
        excludeList.add(DUSERNAME);
        excludeList.add(DPASSWORD);
        excludeList.add(DSERVERLIST);
        
        for(ElementProperty e: elemProp) {
            String propName = e.getAttributeValue("name");
            if (!excludeList.contains(propName)) {
                p.setProperty(propName, e.getAttributeValue("value"));
            }
        }
        logFine("Additional DataSource properties from pool " 
                        + jdbcConPool.getName() + " are " + p);
        return p;
    }

    /**
     * Method to perform any post RA configuration action by derivative subclasses.
     * For example, this method is used by <code>ActiveJMSResourceAdapter</code>
     * to set unsupported javabean property types on its RA JavaBean runtime 
     * instance. 
     * @throws ConnectorRuntimeException
     */
    protected void postRAConfiguration() throws ConnectorRuntimeException {
        //Set all non-supported javabean property types in the JavaBean
        try {
            if (dbProps != null) {
                Method[] mthds = this.resourceadapter_.getClass().getMethods();
                for (int i = 0; i < mthds.length; i++) {
                    if(mthds[i].getName().equalsIgnoreCase("set" + DB_HADB_PROPS)) {
                        logFine("Setting property:" + DB_HADB_PROPS 
                                        + "=" + dbProps.toString());
                        mthds[i].invoke(this.resourceadapter_, 
                                        new Object[]{dbProps});
                    } else if(mthds[i].getName().equalsIgnoreCase("set" + DS_HADB_PROPS)) {
                        logFine("Setting property:" + DS_HADB_PROPS 
                                        + "=" + dsProps.toString());
                        mthds[i].invoke(this.resourceadapter_, new Object[]{dsProps});
                    }
                }
            }
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(
                            e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }
    
    private String getPropertyFromPool(JdbcConnectionPool jdbcConPool, 
                    String poolPropertyName) {
        String poolPropertyValue = null;
        if(jdbcConPool == null) {
            return null;
        }
        ElementProperty[] props = jdbcConPool.getElementProperty();
        for (int i = 0; i < props.length; i++) {
            String name = props[i].getAttributeValue("name");
            String value = props[i].getAttributeValue("value");
            if (name.equalsIgnoreCase(poolPropertyName)) {
            //if (name.equalsIgnoreCase("username")) {
                poolPropertyValue = value; 
            }
        }
        logFine("ActiveJMSResourceAdapter :: got property " + poolPropertyName 
                        + "="+ poolPropertyValue);
        return poolPropertyValue;
    }

    private JdbcConnectionPool getJDBCConnectionPoolInfo(String poolJndiName) 
                                                        throws ConfigException {
        return ResourcesUtil.createInstance().getJDBCPoolForResource(poolJndiName);    
    }

    private boolean isJMSAvailabilityOn(AvailabilityService as) {
        //need to check for global availability like EJB
	/* JMS availability is ON only of AS availability and JMS availability
	 * are on , not otherwise
	*/
	
	if (as == null) {
		return false;
	}
	boolean asAvailability = as.isAvailabilityEnabled();
        JmsAvailability ja = as.getJmsAvailability();
        boolean jmsAvailability = false;
	 /* JMS Availability  should be false if its not present in
 	  * domain.xml, 
	  */
	if (ja != null) {
	    jmsAvailability = ja.isAvailabilityEnabled();
	}
        logFine("JMS availability :: " + (jmsAvailability && asAvailability));
        return (jmsAvailability && asAvailability);
    }

    /**
     * Set MQ4.0 RA lifecycle properties 
     */
    private void setLifecycleProperties() throws 
                                      ConfigException, ConnectorRuntimeException {
        ConfigContext ctx = ApplicationServer.getServerContext().getConfigContext();
    

    	//If PE: 
        //EMBEDDED/LOCAL goto jms-service, get defaultjmshost info and set 
        //accordingly
        //if EE:
        //EMBEDDED/LOCAL get this instance and cluster name, search for a 
        //jms-host wth this this name in jms-service gets its proeprties 
        //and set
        //@siva As of now use default JMS host. As soon as changes for modifying EE
        //cluster to LOCAL is brought in, change this to use system properties
        //for EE to get port, host, adminusername, adminpassword.
        JmsService jmsService = ServerBeansFactory.getJmsServiceBean(ctx);
        String defaultJmsHost = jmsService.getDefaultJmsHost();
        logFine("Default JMS Host :: " + defaultJmsHost);
        
        JmsHost jmsHost = null;
        if (defaultJmsHost == null || defaultJmsHost.equals("")) {
            jmsHost = ServerBeansFactory.getJmsHostBean(ctx);
        } else {
            jmsHost = jmsService.getJmsHostByName(defaultJmsHost);
        }
        
        if (jmsHost != null && jmsHost.isEnabled()) {
            JavaConfig javaConfig = ServerBeansFactory.getJavaConfigBean(ctx);
            String java_home = javaConfig.getJavaHome();
            
            //Get broker type from JMS Service.
            // String brokerType = jmsService.getType();
            /*
             * XXX: adjust the brokertype for the new DIRECT mode in 4.1
             * uncomment the line below once we have an MQ integration
             * that has DIRECT mode support
             */
            String brokerType = adjustForDirectMode(jmsService.getType());
            
            String brokerPort = jmsHost.getPort();
	    brkrPort = brokerPort;
            String adminUserName = jmsHost.getAdminUserName();
            String adminPassword = jmsHost.getAdminPassword();
	    ElementProperty[] jmsHostProps= jmsService.getElementProperty();
	    
	    String username = null;
	    String password = null;
	    if (jmsHostProps != null) {
		for (int i =0;i <jmsHostProps.length; i++) {	
			String propName = jmsHostProps[i].getName();
			String propValue = jmsHostProps[i].getValue();
			if ("user-name".equals(propName)) {
				username = propValue;
			} else if ("password".equals(propName)) {
				password = propValue;
			}
			// Add more properties as and when you want.
		}
	     }
			
	    logFine("Broker UserName = " + username);
            createMQVarDirectoryIfNecessary();
            String brokerVarDir = getMQVarDir(); 

            String tmpString = jmsService.getStartArgs();
            if (tmpString == null) {
                tmpString = "";
            }
            
            String brokerArgs = tmpString;
            

            //XXX: Extract the information from the optional properties.
            ElementProperty[] jmsProperties =
                jmsService.getElementProperty();
            
            String brokerHomeDir = getBrokerHomeDir();
	    String brokerLibDir = getBrokerLibDir();
            if (brokerInstanceName == null) {
                brokerInstanceName = getBrokerInstanceName(jmsService);
            }
            
            long brokerTimeOut = getBrokerTimeOut(jmsService);

            //Need to set the following properties
            //BrokerType, BrokerInstanceName, BrokerPort,
            //BrokerArgs, BrokerHomeDir, BrokerVarDir, BrokerStartTimeout
            //adminUserName, adminPassword
            ConnectorDescriptor cd = getDescriptor();
            EnvironmentProperty envProp1 = new EnvironmentProperty (
                    BROKERTYPE, brokerType, "Broker Type", "java.lang.String");
            setProperty(cd, envProp1);
            EnvironmentProperty envProp2 = new EnvironmentProperty (
                    BROKERINSTANCENAME, brokerInstanceName , 
                    "Broker Instance Name", "java.lang.String");
            setProperty(cd, envProp2);
            EnvironmentProperty envProp3 = new EnvironmentProperty (
                    BROKERPORT , brokerPort , 
                    "Broker Port", "java.lang.String");
            setProperty(cd, envProp3);
            EnvironmentProperty envProp4 = new EnvironmentProperty (
                    BROKERARGS , brokerArgs , 
                    "Broker Args", "java.lang.String");
            setProperty(cd, envProp4);
            EnvironmentProperty envProp5 = new EnvironmentProperty (
                    BROKERHOMEDIR , brokerHomeDir , 
                    "Broker Home Dir", "java.lang.String");
            setProperty(cd, envProp5);
            EnvironmentProperty envProp14 = new EnvironmentProperty (
                    BROKERLIBDIR , brokerLibDir , 
                    "Broker Lib Dir", "java.lang.String");
            setProperty(cd, envProp14);
            EnvironmentProperty envProp6 = new EnvironmentProperty (
                    BROKERJAVADIR , java_home , 
                    "Broker Java Dir", "java.lang.String");
                    setProperty(cd, envProp6);
            EnvironmentProperty envProp7 = new EnvironmentProperty (
                    BROKERVARDIR , brokerVarDir , 
                    "Broker Var Dir", "java.lang.String");
            setProperty(cd, envProp7);
            EnvironmentProperty envProp8 = new EnvironmentProperty (
                    BROKERSTARTTIMEOUT , "" + brokerTimeOut , 
                    "Broker Start Timeout", "java.lang.String");
            setProperty(cd, envProp8);
            EnvironmentProperty envProp9 = new EnvironmentProperty (
                    ADMINUSERNAME , adminUserName, 
                    "Broker admin username", "java.lang.String");
            setProperty(cd, envProp9);
            EnvironmentProperty envProp10 = new EnvironmentProperty (
                    ADMINPASSWORD , adminPassword , 
                    "Broker admin password", "java.lang.String");
            setProperty(cd, envProp10);
            EnvironmentProperty envProp11 = new EnvironmentProperty (
                    USERNAME , username, 
                    "Broker username", "java.lang.String");
            setProperty(cd, envProp11);
            EnvironmentProperty envProp12 = new EnvironmentProperty (
                    PASSWORD , password, 
                    "Broker password", "java.lang.String");
            setProperty(cd, envProp12);
            
            //set adminpassfile
            if (!jmsService.getType().equals(REMOTE)) {
                //For LOCAL and EMBEDDED, we pass in the admin pass file path
                //containing the MQ admin password to enable authenticated 
                //startup of the broker.
                String adminPassFilePath = getAdminPassFilePath(adminPassword);
                if (adminPassFilePath != null) {
                    EnvironmentProperty envProp13 = new EnvironmentProperty (
                            ADMINPASSFILE , adminPassFilePath , 
                            "Broker admin password", "java.lang.String");
                    setProperty(cd, envProp13);
                }
            }
        }
        //Optional
        //BrokerBindAddress, RmiRegistryPort
    }
	
    private String getAdminPassFilePath(String adminPassword) {
        try {
            mqPassFile = File.createTempFile(MQ_PASS_FILE_PREFIX,null);
            BufferedWriter out = new BufferedWriter(new FileWriter(mqPassFile));
            out.write(MQ_PASS_FILE_KEY + adminPassword);
            out.newLine();
            out.flush();
            out.close();
            return mqPassFile.getCanonicalPath();
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException while creating MQ admin pass file" + e.getMessage());
        }
        return null;
    }
    
    private String adjustForDirectMode(String brokerType) {
    	if (brokerType.equals(EMBEDDED)) {
    		String revertToEmbedded = System.getProperty(REVERT_TO_EMBEDDED_PROPERTY);
    		if ((revertToEmbedded != null) && (revertToEmbedded.equals("true"))){
    			return EMBEDDED;
    		}
    		return DIRECT;
    	}
    	return brokerType;
    }

    private long getBrokerTimeOut(JmsService jmsService) {
        //@@remove
        long defaultTimeout = 30 * 1000; //30 seconds
        long timeout = defaultTimeout;
    
        String specifiedTimeOut = jmsService.getInitTimeoutInSeconds(); 
        if (specifiedTimeOut != null)
            timeout = Integer.parseInt(specifiedTimeOut) * 1000;
        return timeout;
    }

    public static String getBrokerInstanceName(JmsService js) 
                      throws ConfigException, ConnectorRuntimeException {
        String asInstance = ApplicationServer.getServerContext().getInstanceName();
        String domainName = null;
        if (isClustered()) {
            domainName = ClusterHelper.getClusterForInstance(
                            ApplicationServer.getServerContext().getConfigContext(), 
                            asInstance).getName();
        } else { 
            domainName = ServerManager.instance().getDomainName();
        }
        String s = IASJmsUtil.getBrokerInstanceName(domainName, asInstance, js);
        logFine("IASJMSUtil gave broker Instancename as " + s);
        String converted = convertStringToValidMQIdentifier(s);
        logFine("converted instance name " + converted);
        return converted;
    }

    private void createMQVarDirectoryIfNecessary(){
        String asInstanceRoot = ApplicationServer.getServerContext().
                                   getInstanceEnvironment().getInstancesRoot(); 
        String mqInstanceDir =  asInstanceRoot + java.io.File.separator 
    	                                          + IASJmsUtil.MQ_DIR_NAME;
    	 // If the directory doesnt exist, create it.
    	 // It is necessary for windows.
    	 java.io.File instanceDir = new java.io.File(mqInstanceDir);
    	 if (!(instanceDir.exists() && instanceDir.isDirectory())) {
    	     instanceDir.mkdirs();
    	 }
    }
    
    private String getMQVarDir(){
        String asInstanceRoot = ApplicationServer.getServerContext().
                                  getInstanceEnvironment().getInstancesRoot();
        String mqInstanceDir =  asInstanceRoot + java.io.File.separator
                                                 + IASJmsUtil.MQ_DIR_NAME;
        return mqInstanceDir;
    }
    private String getBrokerLibDir() {
        String brokerLibDir = java.lang.System.getProperty(SystemPropertyConstants.IMQ_LIB_PROPERTY);
        logFine("broker lib dir from system property " + brokerLibDir);
	return brokerLibDir;
   }
	 
    private String getBrokerHomeDir() {
        // If the property was not specified, then look for the 
        // imqRoot as defined by the com.sun.aas.imqRoot property   
        String brokerHomeDir = java.lang.System.getProperty(SystemPropertyConstants.IMQ_BIN_PROPERTY);
        logFine("broker home dir from system property " + brokerHomeDir);
        
        // Finally if all else fails (though this should never happen)
        // look for IMQ relative to the installation directory
        //@todo reget brokerHomeDir
        if (brokerHomeDir == null) {
            String IMQ_INSTALL_SUBDIR = java.io.File.separator + 
                ".." + java.io.File.separator + ".." +
                java.io.File.separator + "imq" ;
                //java.io.File.separator + "bin"; hack until MQ RA changes
            //XXX: This doesn't work in clustered instances.
            brokerHomeDir = ApplicationServer.getServerContext().getInstallRoot() 
            					+ IMQ_INSTALL_SUBDIR;
        } else {
            //hack until MQ RA changes
            brokerHomeDir = brokerHomeDir + java.io.File.separator + ".." ; 
        }
        
        logFine("Broker Home Directory :: " + brokerHomeDir);
        logFine("broker home dir finally" + brokerHomeDir);
        return brokerHomeDir;
    	
    }
    
    ////@Siva: provide an API to read JMX information from RA and return it.
    //private 

    

    /**
     * Sets the SE/EE specific MQ-RA bean properties 
     * @throws ConnectorRuntimeException
     */
    private void setClusterRABeanProperties() throws ConnectorRuntimeException {
        ConnectorDescriptor cd = super.getDescriptor();
        try {
            if (isClustered()) {
                ConfigContext ctx = ApplicationServer.getServerContext().
					getConfigContext();
	        JmsService jmsService = ServerBeansFactory.
				getConfigBean(ctx).getJmsService();
                String val = getGroupName();
                EnvironmentProperty envProp = new EnvironmentProperty
                    (GROUPNAME, val, "Group Name", "java.lang.String");
                setProperty(cd, envProp);
                EnvironmentProperty envProp1 = new EnvironmentProperty
                  (CLUSTERCONTAINER, "true", "Cluster container flag", 
                    "java.lang.Boolean");
                setProperty(cd, envProp1);
                logFine("CLUSTERED instance - setting groupname as" 
	             + val);	
		if (jmsService.getType().equals(REMOTE)) {	
		
		    /*
		     * Do not set master broker for remote broker.
		     * The RA might ignore it if we set, but we have to 
                     * be certain from our end.
		     */
                     return;
		} else {
	            String masterbrkr = getMasterBroker();
                    EnvironmentProperty envProp2 = new EnvironmentProperty
                        (MASTERBROKER,masterbrkr , "Master  Broker", 
	                "java.lang.String");
                    setProperty(cd, envProp2);
                    logFine("MASTERBROKER - setting master broker val" 
	                + masterbrkr);
		}
            } else {
                logFine("Instance not Clustered and hence not setting " +
                        "groupname");
            }
        } catch (ConfigException e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }
    
    /**
     * Sets the SE/EE specific MQ-RA bean properties 
     * @throws ConnectorRuntimeException
     */
    private void setAppClientRABeanProperties() throws ConnectorRuntimeException {
        logFine("In Appclient container!!!");
        ConnectorDescriptor cd = super.getDescriptor();
        EnvironmentProperty envProp1 = new EnvironmentProperty (
                        BROKERTYPE, REMOTE, "Broker Type", "java.lang.String");
                setProperty(cd, envProp1);
        
        EnvironmentProperty envProp2 = new EnvironmentProperty (
            GROUPNAME, "", "Group Name", "java.lang.String");
        cd.removeConfigProperty(envProp2);
        EnvironmentProperty envProp3 = new EnvironmentProperty (
            CLUSTERCONTAINER, "false", "Cluster flag", "java.lang.Boolean");
        setProperty(cd, envProp3);
    }
    
    
    private static boolean isClustered() throws ConnectorRuntimeException {
        return JmsRaUtil.isClustered();
    }
    
    private String getGroupName() throws ConfigException{
        return getDomainName() + SEPARATOR + getClusterName();
    }
    
    private String getClusterName() throws ConfigException {
        return ClusterHelper.getClusterForInstance(
                        ApplicationServer.getServerContext().getConfigContext(), 
                        ApplicationServer.getServerContext().getInstanceName()).getName();
    }
    
    /*
     * Generates an Name for the MQ Cluster associated with the 
     * application server cluster.
     */ 
    private String getMQClusterName() throws ConfigException {
        return convertStringToValidMQIdentifier(getClusterName()) + "_MQ"; 
    }
   
    /**
     * Master Broker name in the cluster, assumes the first broker in
     * in the list is the master broker , and this consistency has to 
     * be maintained in all the instances.
     */
     private String getMasterBroker() throws ConfigException {
	return urlList.getMasterBroker(getClusterName());
     }
	 
    //All Names passed into MQ needs to be valid Java Identifiers
    //so as of now replacing all characters that are not valid
    //java identifier components with '_'
    private static String convertStringToValidMQIdentifier(String s) {
        if (s == null) return "";
        
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < s.length(); i++) {
            if(Character.isLetterOrDigit(s.charAt(i))){ 
                            //|| s.charAt(i) == '_'){
                buf.append(s.charAt(i));
            } 
        }
        return buf.toString();
    }
    
    private String getDomainName() throws ConfigException {
        ConfigContext ctxt = ApplicationServer.getServerContext().getConfigContext();
        //computing hashcode, since the application root string could
        //be potentially large
        /*
        String domainName = "" + ServerBeansFactory.getDomainBean(ctxt).
                            getApplicationRoot().hashCode();
        return domainName;
        */
        // FIX LATER
        return "";
    }

    /**
     * Recreates the ResourceAdapter using new values from JmsSerice.
     *
     * @param js JmsService element of the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void reloadRA(JmsService js) throws ConnectorRuntimeException {
        setMdbContainerProperties();
        setJmsServiceProperties(js);
     
        super.loadRAConfiguration();
	rebindDescriptor();
    }

    /**
     * Adds the JmsHost to the MQAddressList of the resource adapter.
     * 
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void addJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.addMQUrl(host);
        setAddressList();
    }

    /**
     * Removes the JmsHost from the MQAddressList of the resource adapter.
     * 
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void deleteJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.removeMQUrl(host);
        setAddressList();
    }

    /**
     * Updates the JmsHost information in the MQAddressList of the resource adapter.
     * 
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void updateJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.updateMQUrl(host);
        setAddressList();
    }
    
    private void setMdbContainerProperties() throws ConnectorRuntimeException {
        JmsRaUtil raUtil = new JmsRaUtil(null);
				       
        ConnectorDescriptor cd = super.getDescriptor();
        raUtil.setMdbContainerProperties();

        String val = ""+MdbContainerProps.getReconnectEnabled();
        EnvironmentProperty envProp2 = new EnvironmentProperty (
            RECONNECTENABLED, val, val, "java.lang.Boolean");
        setProperty(cd, envProp2); 

        val = ""+MdbContainerProps.getReconnectDelay(); 
        EnvironmentProperty envProp3 = new EnvironmentProperty (
            RECONNECTINTERVAL, val, val, "java.lang.Integer");
        setProperty(cd, envProp3); 

        val = ""+MdbContainerProps.getReconnectMaxRetries(); 
        EnvironmentProperty envProp4 = new EnvironmentProperty (
            RECONNECTATTEMPTS, val, val, "java.lang.Integer");
        setProperty(cd, envProp4); 

	// The above properties will be set in ConnectorDescriptor and
	// will be bound in JNDI. This will be available to appclient
	// and standalone client.
    }

    private void setAddressList() throws ConnectorRuntimeException {
        //@Siva: Enhance setting AddressList. [Ignore this machines jms-host while 
        //constructing addresslist]
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().getConfigContext();
            JmsService jmsService = ServerBeansFactory.getJmsServiceBean(ctx);
            setConnectionURL(jmsService, urlList);
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        super.loadRAConfiguration();
    }

    //This is a MQ workaround. In PE, when the broker type is
    //EMBEDDED or LOCAL, do not set the addresslist, else
    //MQ RA assumes that there are two URLs and fails (EE limitation).
    private void setConnectionURL(JmsService jmsService, MQAddressList urlList) {
        ConnectorDescriptor cd = super.getDescriptor();
        String val = urlList.toString();
        if (val != null) {
            logger.info("JMS Service Connection URL is :" + val);
            EnvironmentProperty envProp1 = new EnvironmentProperty (
               CONNECTION_URL, val, val, "java.lang.String");
            setProperty(cd, envProp1); 
        }
    }

    private void setJmsServiceProperties(JmsService service) throws 
                                         ConnectorRuntimeException {
        JmsRaUtil jmsraUtil = new JmsRaUtil(service);
	jmsraUtil.setupAddressList();
	urlList = jmsraUtil.getUrlList();
	addressList = urlList.toString();
        ConnectorDescriptor cd = super.getDescriptor();
        setConnectionURL(service, urlList);

        String val = ""+jmsraUtil.getReconnectEnabled();
        EnvironmentProperty envProp2 = new EnvironmentProperty (
            RECONNECTENABLED, val, val, "java.lang.Boolean");
        setProperty(cd, envProp2); 

        //convert to milliseconds
        int newval = (new Integer(jmsraUtil.getReconnectInterval())).intValue() * 1000;
        val = "" + newval;  
        EnvironmentProperty envProp3 = new EnvironmentProperty (
            RECONNECTINTERVAL, val, val, "java.lang.Integer");
        setProperty(cd, envProp3); 

        val = ""+jmsraUtil.getReconnectAttempts(); 
        EnvironmentProperty envProp4 = new EnvironmentProperty (
            RECONNECTATTEMPTS, val, val, "java.lang.Integer");
        setProperty(cd, envProp4); 

        val = ""+jmsraUtil.getAddressListBehaviour(); 
        EnvironmentProperty envProp5 = new EnvironmentProperty (
            ADRLIST_BEHAVIOUR, val, val, "java.lang.String");
        setProperty(cd, envProp5); 

        val = ""+jmsraUtil.getAddressListIterations(); 
        EnvironmentProperty envProp6 = new EnvironmentProperty (
            ADRLIST_ITERATIONS, val, val, "java.lang.Integer");
        setProperty(cd, envProp6); 
        
        boolean useExternal = shouldUseExternalRmiRegistry(jmsraUtil);
        val = (new Boolean(useExternal)).toString();
        EnvironmentProperty envProp7 = new EnvironmentProperty (
            USEEXTERNALRMIREGISTRY, val, val, "java.lang.Boolean");
        setProperty(cd, envProp7); 

        logger.log(Level.FINE, "Start RMI registry set as "+ val);
        //If MQ RA needs to use AS RMI Registry Port, then set
        //the RMI registry port, else MQ RA uses its default RMI
        //Registry port  [as of now 1099]
        String configuredRmiRegistryPort = null ;
        if (!useExternal) {
            configuredRmiRegistryPort = getRmiRegistryPort();
        } else {
		/* We will be here if we are LOCAL or REMOTE, standalone
		 * or clustered. We could set the Rmi registry port.
		 * The RA should ignore the port if REMOTE and use it only
		 * for LOCAL cases.
		 */	
            configuredRmiRegistryPort = getUniqueRmiRegistryPort();
	}
        val = configuredRmiRegistryPort;
        if (val != null) {
            EnvironmentProperty envProp8 = new EnvironmentProperty (
                RMIREGISTRYPORT, val, val, "java.lang.Integer");
            setProperty(cd, envProp8);
            logger.log(Level.FINE, "RMI registry port set as "+ val);
        } else {
            logger.log(Level.WARNING, "Invalid RMI registry port");
        }
    }

    /*
     * Checks if AS RMI registry is started and available for use.
     */
    private boolean shouldUseExternalRmiRegistry (JmsRaUtil jmsraUtil) {
        boolean useExternalRmiRegistry = ( !isASRmiRegistryPortAvailable(jmsraUtil) );
        //System.out.println("========== " + useExternalRmiRegistry);
        return useExternalRmiRegistry;
    }

    /** 
     * This method should return a unique and unused port , so that
     * the broker can use this to start its Rmi registry.
     * Used only for LOCAL mode
     */
    private String getUniqueRmiRegistryPort() {
	int mqrmiport  = DEFAULTRMIREGISTRYPORT;
	try {
		String configuredport = System.getProperty(BROKER_RMI_PORT);
		if (configuredport != null) {
		    mqrmiport = Integer.parseInt(configuredport);
		} else { 
		    mqrmiport = Integer.parseInt(brkrPort) 
                                    + BROKERRMIPORTOFFSET;
		}	
	} catch (Exception e) {
		;
	}
	return "" + mqrmiport;
    }

    /**
     * Get the AS RMI registry port for MQ RA to use.
     */
    private String getRmiRegistryPort() {
        String val = null;
        if (MQRmiPort != null && !MQRmiPort.trim().equals("")){
            return MQRmiPort;
        } else {
            String configuredPort = null;
            try {
                configuredPort = getConfiguredRmiRegistryPort();
            } catch (ConfigException ex) {
                logger.log(Level.WARNING, ex.getMessage());
                logger.log(Level.FINE, "Exception while getting configured rmi " +
                                                 "registry port", ex);
            }
            if (configuredPort != null) {
                return configuredPort;
            } 
            
            //Finally if DAS and configured port doesn't work, return DAS'
            //RMI registry port as a fallback option.
            if (ResourcesUtil.isDAS()) {
                    return DASRMIPORT;
            }
        }
        return val;
    }
    
    private String getConfiguredRmiRegistryHost() throws ConfigException {
        return getJmxConnector().getAddress();
    }

    private String getConfiguredRmiRegistryPort() throws ConfigException {
        return getJmxConnector().getPort();
    }
    
    private JmxConnector getJmxConnector() throws ConfigException{
        AdminService as = ServerBeansFactory.getConfigBean(
                        ApplicationServer.getServerContext().getConfigContext()).getAdminService();
        return as.getJmxConnectorByName(as.getSystemJmxConnectorName());
    }
    
    private boolean isASRmiRegistryPortAvailable(JmsRaUtil jmsraUtil) {
        logFine("isASRmiRegistryPortAvailable - JMSService Type:" + jmsraUtil.getJMSServiceType());
 	//If JMSServiceType is REMOTE, then we need not ask the MQ RA to use the 
 	//AS RMI Registry. So the check below is not necessary.
 	if (jmsraUtil.getJMSServiceType().equals(REMOTE) 
	    || jmsraUtil.getJMSServiceType().equals(LOCAL)) {
 	    return false;
 	}

        String name = null;
        try {
           //Attempt to connect to the RMI registry
            name = "rmi://"+getConfiguredRmiRegistryHost() + ":" + getConfiguredRmiRegistryPort();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Attempting to list " + name);
            }
            String[] ss = Naming.list(name);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("List on " + name + " succeeded");
            }
            //return configured port only if RMI registry is available
            return true;
        } catch (Exception e) {
            logger.fine(e.getMessage() + " " + name);
            return false;
        }
    }
    
    private void setProperty(ConnectorDescriptor cd, EnvironmentProperty envProp){
        cd.removeConfigProperty(envProp);
        cd.addConfigProperty(envProp);
    }


    private void rebindDescriptor() throws ConnectorRuntimeException {
        try {
            String descriptorJNDIName = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForDescriptor(super.getModuleName());
	    com.sun.enterprise.Switch.getSwitch().getNamingManager().
	    publishObject( descriptorJNDIName, super.getDescriptor(), true);
	} catch (javax.naming.NamingException ne) {
	    ConnectorRuntimeException cre = new ConnectorRuntimeException (ne.getMessage());
	    throw (ConnectorRuntimeException) cre.initCause(ne);
	}
    }
	
    /**
     * This is a temporay solution for obtaining all the MCFs 
     * corresponding to a JMS RA pool, this is to facilitate the 
     * recovery process where the XA resources of all RMs in the 
     * broker cluster are required. Should be removed when a permanent
     * solutuion is available from the broker.
     * @param cpr <code>ConnectorConnectionPool</code> object
     * @parm loader Class Loader.
     * @throw ConnectorRuntimeException in case of an exception.
     */	
   public ManagedConnectionFactory [] createManagedConnectionFactories
		(com.sun.enterprise.connectors.ConnectorConnectionPool cpr, 
                                                        ClassLoader loader) {
   	    logger.log(Level.FINE,"RECOVERY : Entering createMCFS in AJMSRA");
	    ArrayList mcfs = new ArrayList();	
            if (getAddressListCount() < 2) {
		mcfs.add(createManagedConnectionFactory(cpr,loader));
		logger.log(Level.FINE,"Brokers are not clustered,So doing normal recovery");
	    } else {
	    ArrayList al = new ArrayList();
	    String addlist = null;
   	    Set s = cpr.getConnectorDescriptorInfo().getMCFConfigProperties();
            Iterator tmpit = s.iterator();
            while (tmpit.hasNext()) {
                EnvironmentProperty prop = (EnvironmentProperty) tmpit.next();
                String propName = prop.getName();
                if (propName.equalsIgnoreCase("imqAddressList") || propName.equalsIgnoreCase("Addresslist")) {
		    addlist = prop.getValue();
		}
	    }
	    StringTokenizer tokenizer = null;
	    if ((addlist == null) 
		|| (addlist.trim().equalsIgnoreCase("localhost"))) {
	        tokenizer = new StringTokenizer(addressList, ",");
	    }else {
            	tokenizer = new StringTokenizer(addlist, ",");
	    }
	   logger.log(Level.FINE, "No of addresses found " + 
			tokenizer.countTokens());
	    while (tokenizer.hasMoreTokens()) { 
		String brokerurl = tokenizer.nextToken();
        	ManagedConnectionFactory mcf = super.
              	  createManagedConnectionFactory(cpr, loader);
            	Iterator it = s.iterator();
            	while (it.hasNext()) {
               	 EnvironmentProperty prop = (EnvironmentProperty) it.next();
               	 String propName = prop.getName();
		 String propValue = prop.getValue();
                 if (propName.startsWith("imq") && propValue != "") {
                  try {
                 	Method meth = mcf.getClass().getMethod
                       	(SETTER, new  Class[] {java.lang.String.class, 
                              java.lang.String.class});
                	if (propName.trim().equalsIgnoreCase("imqAddressList")){
                     		meth.invoke(mcf, new Object[] {prop.getName(),brokerurl});
			} else {	
                     		meth.invoke(mcf, new Object[] {prop.getName(),prop.getValueObject()});
			}
                   } catch (NoSuchMethodException ex) {
                   	logger.log(Level.WARNING, "no.such.method", 
                       	new Object[] {SETTER, mcf.getClass().getName()});
                   } catch (Exception ex) {
                   	logger.log(Level.SEVERE, "error.execute.method", 
                       	new Object[] {SETTER, mcf.getClass().getName()});
		   }
		}
              }
              EnvironmentProperty addressProp3 = new EnvironmentProperty (                                    ADDRESSLIST, brokerurl,"Address List",
                            "java.lang.String");
	      HashSet addressProp = new HashSet();
		addressProp.add(addressProp3);
	      SetMethodAction setMethodAction = 
			new SetMethodAction(mcf,addressProp);
	      try {
             	    setMethodAction.run();
		} catch (Exception e) {
			;
		}
		mcfs.add(mcf);
	}
	}
	return (ManagedConnectionFactory [])mcfs.toArray(new ManagedConnectionFactory[0]);
    }

    /**
     * Creates ManagedConnection Factory instance. For any property that is 
     * for supporting AS7 imq properties, resource adapter has a set method
     * setProperty(String,String). All as7 properties starts with "imq".
     * MQ Adapter supports this only for backward compatibility.
     * 
     * @param cpr <code>ConnectorConnectionPool</code> object
     * @parm loader Class Loader.
     * @throw ConnectorRuntimeException in case of an exception.
     */
    public ManagedConnectionFactory createManagedConnectionFactory
       	       (com.sun.enterprise.connectors.ConnectorConnectionPool cpr, 
                                                       ClassLoader loader) {
        ManagedConnectionFactory mcf = 
            super.createManagedConnectionFactory(cpr, loader);
        if ( mcf != null ) {
            Set s = cpr.getConnectorDescriptorInfo().getMCFConfigProperties();
            Iterator it = s.iterator();
            while (it.hasNext()) {
                EnvironmentProperty prop = (EnvironmentProperty) it.next();
                String propName = prop.getName();
           
                // If the property has started with imq, then it should go to
                // setProperty(String,String) method.
                if (propName.startsWith("imq") && prop.getValue() != "") {
                    try {
                        Method meth = mcf.getClass().getMethod
                        (SETTER, new  Class[] {java.lang.String.class, 
                                               java.lang.String.class});
                        meth.invoke(mcf, new Object[] {prop.getName(),
                                                        prop.getValueObject()});
                    } catch (NoSuchMethodException ex) {
                        logger.log(Level.WARNING, "no.such.method", 
                        new Object[] {SETTER, mcf.getClass().getName()});
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "error.execute.method", 
                        new Object[] {SETTER, mcf.getClass().getName()});
                    }
	        }
            }
                
        }
        return mcf;
    }

    /**
     * This is the most appropriate time (??) to update the runtime
     * info of a 1.3 MDB into 1.4 MDB.  <p>
     *
     * Assumptions : <p>
     * 0. Assume it is a 1.3 MDB if no RA mid is specified.
     * 1. Use the default system JMS resource adapter. <p>
     * 2. The ActivationSpec of the default JMS RA will provide the
     *    setDestination, setDestinationType, setSubscriptionName methods.
     * 3. The jndi-name of the 1.3 MDB is the value for the Destination
     *    property for the ActivationSpec.
     * 4. The ActivationSpec provides setter methods for the properties
     *    defined in the CF that corresponds to the mdb-connection-factory
     *    JNDI name.
     *
     */
    public void updateMDBRuntimeInfo(EjbMessageBeanDescriptor descriptor_,
           BeanPoolDescriptor poolDescriptor) throws ConnectorRuntimeException{

        String jndiName = descriptor_.getJndiName();
        
        //handling of MDB 1.3 runtime deployment descriptor
        //if no RA-mid is specified, assume it is a 1.3 DD
        if (jndiName == null) { //something's wrong in DD
            logger.log (Level.SEVERE, "Missing Destination JNDI Name");
	    String msg = sm.getString("ajra.error_in_dd");
            throw new RuntimeException(msg);
        }
        
        String resourceAdapterMid = ConnectorRuntime.DEFAULT_JMS_ADAPTER;
        
        descriptor_.setResourceAdapterMid(resourceAdapterMid);


        String destName = getPhysicalDestinationFromConfiguration(jndiName);
        
        //1.3 jndi-name ==> 1.4 setDestination
        descriptor_.putRuntimeActivationConfigProperty(
                new EnvironmentProperty(DESTINATION, 
                        destName, null));
	
        
        //1.3 (standard) destination-type == 1.4 setDestinationType
        //XXX Do we really need this???
        if (descriptor_.getDestinationType() != null &&
                !"".equals(descriptor_.getDestinationType())) {
            descriptor_.putRuntimeActivationConfigProperty(
                    new EnvironmentProperty(DESTINATION_TYPE, 
                            descriptor_.getDestinationType(), null));
        } else {
            /*
             * If destination type is not provided by the MDB component
             * [typically used by EJB3.0 styled MDBs which create MDBs without
             * a destination type activation-config property] and the MDB is for
             * the default JMS RA, attempt to infer the destination type by trying
             * to find out if there has been any JMS destination resource already
             * defined for default JMS RA. This is a best attempt guess and if there
             * are no JMS destination resources/admin-objects defined, AS would pass
             * the properties as defined by the MDB.
             */
            ConfigBean[] cb;
            try {
                cb = ResourcesUtil.createInstance().getEnabledAdminObjectResources(
                        ConnectorConstants.DEFAULT_JMS_ADAPTER);
                for (int i = 0; i < cb.length; i++) {
                    AdminObjectResource aor = (AdminObjectResource) cb[i];
                    if (aor.getJndiName().equals(jndiName)) {
                        descriptor_.putRuntimeActivationConfigProperty(
                                new EnvironmentProperty(DESTINATION_TYPE, 
                                        aor.getResType(), null));
                        logger.log(Level.INFO, "endpoint.determine.destinationtype", new 
                                Object[]{aor.getResType() , aor.getJndiName() , descriptor_.getName()});                        
                    }
                }
            } catch (ConfigException e) {
                
            }
        }
        
        
        //1.3 durable-subscription-name == 1.4 setSubscriptionName
        descriptor_.putRuntimeActivationConfigProperty(
                new EnvironmentProperty(SUBSCRIPTION_NAME,
                        descriptor_.getDurableSubscriptionName(), null));

        String mdbCF = null;
	try {
	    mdbCF = descriptor_.getIASEjbExtraDescriptors().
                    getMdbConnectionFactory().getJndiName();
	} catch(NullPointerException ne ) {
	    // Dont process connection factory.
	}

        if (mdbCF != null && mdbCF != "") {
	    setValuesFromConfiguration(mdbCF, descriptor_);
        }

        // a null object is passes as a PoolDescriptor during recovery.
        // See com/sun/enterprise/resource/ResourceInstaller
        
        if (poolDescriptor != null) {
        descriptor_.putRuntimeActivationConfigProperty
            (new EnvironmentProperty (MAXPOOLSIZE, ""+
                 poolDescriptor.getMaxPoolSize(),"", "java.lang.Integer" ));
        descriptor_.putRuntimeActivationConfigProperty
            (new EnvironmentProperty (MINPOOLSIZE,""+ 
                 poolDescriptor.getSteadyPoolSize(),"", "java.lang.Integer"));
        descriptor_.putRuntimeActivationConfigProperty
            (new EnvironmentProperty (RESIZECOUNT,""+ 
                 poolDescriptor.getPoolResizeQuantity(),"", "java.lang.Integer"));
        descriptor_.putRuntimeActivationConfigProperty
            (new EnvironmentProperty (RESIZETIMEOUT,""+ 
                 poolDescriptor.getPoolIdleTimeoutInSeconds(),"", "java.lang.Integer"));
        descriptor_.putRuntimeActivationConfigProperty
            (new EnvironmentProperty (REDELIVERYCOUNT,""+ 
                 MdbContainerProps.getMaxRuntimeExceptions(),"", "java.lang.Integer"));
        }
        
        //Set SE/EE specific MQ-RA ActivationSpec properties
        try {
            boolean clustered = isClustered();
            logFine("Are we in a Clustered contained ? " + clustered);
            if (clustered) {
                setClusterActivationSpecProperties(descriptor_);
                logFine("Creating physical destination " + destName);
                logFine("Destination is Queue? " + descriptor_.hasQueueDest());
                if (descriptor_.hasQueueDest()) {
                    autoCreatePhysicalDest(destName, true);
                } else {
                    autoCreatePhysicalDest(destName, false);
                }
            } 
        } catch (ConfigException e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }

    void autoCreatePhysicalDest(String destName, boolean isQueue) 
                                throws ConnectorRuntimeException{
        MQAdministrator mqAdmin = new MQAdministrator();
        mqAdmin.createPhysicalDestination(destName, isQueue);
    }

    /**
     * Set SE/EE specific MQ-RA ActivationSpec properties
     * @param descriptor_
     * @throws ConfigException
     */
    private void setClusterActivationSpecProperties(EjbMessageBeanDescriptor 
                    descriptor_) throws ConfigException {
        //Set MDB Identifier in a clustered instance.
        descriptor_.putRuntimeActivationConfigProperty(new 
                        EnvironmentProperty(MDBIDENTIFIER,""+ 
                        getMDBIdentifier(descriptor_),"MDB Identifier", 
                        "java.lang.String"));
        logFine("CLUSTERED instance - setting MDB identifier as" + 
                        getMDBIdentifier(descriptor_));                
        
    }

    /**
     * Gets the MDBIdentifier for the message bean endpoint
     * @param descriptor_
     * @return
     * @throws ConfigException
     */
    private String getMDBIdentifier(EjbMessageBeanDescriptor descriptor_) throws ConfigException {
        return getDomainName() + SEPARATOR + getClusterName() + SEPARATOR + descriptor_.getUniqueId() ;
    }

    private String getPhysicalDestinationFromConfiguration(String logicalDest)
                                throws ConnectorRuntimeException{
	ElementProperty ep = null;
        try {
            ServerContext sc = ApplicationServer.getServerContext();
            ConfigContext ctx = sc.getConfigContext();
            Resources rbeans = 
                           ServerBeansFactory.getDomainBean(ctx).getResources();
            AdminObjectResource res = (AdminObjectResource) 
		           rbeans.getAdminObjectResourceByJndiName(logicalDest);
	    if (res == null) {
	        String msg = sm.getString("ajra.err_getting_dest", logicalDest );
		throw new ConnectorRuntimeException(msg);
	    }
		         
	    ep = res.getElementPropertyByName(PHYSICAL_DESTINATION);
        } catch(ConfigException ce) {
	    String msg = sm.getString("ajra.err_getting_dest", logicalDest);
	    ConnectorRuntimeException cre = new ConnectorRuntimeException( msg );
	    cre.initCause( ce );
            throw cre;
        }

        if (ep == null) {
	   String msg = sm.getString("ajra.cannot_find_phy_dest", ep);
           throw new ConnectorRuntimeException(msg);
        }

        return ep.getValue();
    }


    private void setValuesFromConfiguration(String cfName, EjbMessageBeanDescriptor 
                                     descriptor_) throws ConnectorRuntimeException{
	ElementProperty[] ep = null;
        try {
            ServerContext sc = ApplicationServer.getServerContext();
            ConfigContext ctx = sc.getConfigContext();
            Resources rbeans = ServerBeansFactory.getDomainBean(ctx).getResources();
            ConnectorResource res = (ConnectorResource) 
		                     rbeans.getConnectorResourceByJndiName(cfName);
	    if (res == null) {
	        String msg = sm.getString("ajra.mdb_cf_not_created", cfName);
		throw new ConnectorRuntimeException(msg);
	    }

	    ConnectorConnectionPool ccp = (ConnectorConnectionPool) 
		    rbeans.getConnectorConnectionPoolByName(res.getPoolName());
		         
	    ep = ccp.getElementProperty();
        } catch(ConfigException ce) {
	    String msg = sm.getString("ajra.mdb_cf_not_created", cfName);
	    ConnectorRuntimeException cre = new ConnectorRuntimeException( msg );
	    ce.initCause( ce );
            throw cre;
        }

        if (ep == null) {
	    String msg = sm.getString("ajra.cannot_find_phy_dest");
            throw new ConnectorRuntimeException( msg );
        }

	for (int i=0; i < ep.length; i++) {
	    ElementProperty prop = ep[i];
	    String name = prop.getName();
	    if (name.equals(MCFADDRESSLIST)) {
	        name = ADDRESSLIST;
	    }
	    String val = prop.getValue();
	    if (val == null || val.equals("")) {
		continue;
	    }
            descriptor_.putRuntimeActivationConfigProperty(
                new EnvironmentProperty(name, val, null));
	}

    }
    
    private static void logFine(String s) {
    	if (logger.isLoggable(Level.FINE)){
    		logger.fine(s);
    	}
    }
  
    public int getAddressListCount() {
	StringTokenizer tokenizer = null;
	int count = 1;
	if (addressList != null) {	
            tokenizer = new StringTokenizer(addressList, ",");
	    count = tokenizer.countTokens();
	}
	logFine("Address list count is " + count);
	return count;
    }
}
