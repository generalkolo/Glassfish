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
package com.sun.ejb.containers;

import java.util.Date;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.Serializable;

import com.sun.logging.LogDomains;
import com.sun.ejb.ContainerFactory;

import javax.ejb.Timer;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.RequestType;
import com.sun.enterprise.Switch;
import com.sun.enterprise.deployment.*;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.ServerManager;

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.EjbTimerService;

import javax.transaction.TransactionManager;

/*
 * EJBTimerService is the central controller of the EJB timer service.  
 * There is one instance of EJBTimerService per VM. All operations and
 * state transitions on timers pass through EJB Timer Service.  This
 * reduces the overall complexity by encapsulating the timer logic
 * within this class and keeping the supporting classes simple.
 *
 * @author Kenneth Saks
 */
public class EJBTimerService 
implements com.sun.ejb.spi.distributed.DistributedEJBTimerService
{
    private long nextTimerIdMillis_ = 0;
    private long nextTimerIdCounter_ = 0;
    private String serverName_;
    private String domainName_;

    // @@@ Double-check that the individual server id, domain name, 
    // and cluster name cannot contain the TIMER_ID_SEP 
    // characters.      

    // separator between components that make up timer id and owner id
    private static final String TIMER_ID_SEP = "@@";

    // Owner id of the server instance in which we are currently running.
    private String ownerIdOfThisServer_;        

    // A cache of timer info for all timers *owned* by this server instance. 
    private TimerCache timerCache_;

    private TimerLocalHome timerLocalHome_;
    private TimerMigrationLocalHome timerMigrationLocalHome_;
    private boolean shutdown_;

    // Total number of ejb components initialized as timed objects between the
    // start and end of a single server instance.  This value is used during
    // restoreTimers() as an optimization to avoid initialization overhead in
    // the common case that there are no applications with timed objects.  
    // It is NOT intended to be a consistent count of the current number of 
    // timed objects, so there is no need to decrement the number when a 
    // container is undeployed.
    private long totalTimedObjectsInitialized_ = 0;

    private static final Logger logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    // Defaults for configurable timer service properties.

    private static final long MINIMUM_DELIVERY_INTERVAL = 7000;
    private static final int MAX_REDELIVERIES = 1;
    private static final long REDELIVERY_INTERVAL = 5000;

    private String appID;

    // minimum amount of time between either a timer creation and its first 
    // expiration or between subsequent timer expirations.  
    private long minimumDeliveryInterval_ = MINIMUM_DELIVERY_INTERVAL;

    // maximum number of times the container will attempt to retry a 
    // timer delivery before giving up.
    private long maxRedeliveries_         = MAX_REDELIVERIES;

    // amount of time the container waits between timer redelivery attempts.
    private long redeliveryInterval_      = REDELIVERY_INTERVAL;

    private static final String TIMER_SERVICE_FILE =
        "__timer_service_shutdown__.dat";
    private static final String TIMER_SERVICE_DOWNTIME_FORMAT =
        "yyyy/MM/dd HH:mm:ss";

    // This boolean value would be set in PE to be a default value of false.
    // In case of EE the default value would be true. When set to true the 
    // timer service would have maximim consistency with performance degration.
    private boolean performDBReadBeforeTimeout = false;
    
    private static final String strDBReadBeforeTimeout = 
        "com.sun.ejb.timer.ReadDBBeforeTimeout";
    private boolean foundSysPropDBReadBeforeTimeout = false;

    public EJBTimerService(String appID, TimerLocalHome timerLocalHome,
                           TimerMigrationLocalHome timerMigrationLocalHome)
    {
        timerLocalHome_ = timerLocalHome;
        timerMigrationLocalHome_ = timerMigrationLocalHome;
        timerCache_     = new TimerCache();
        shutdown_       = false;
        this.appID = appID;

        domainName_ = ServerManager.instance().getDomainName();
        InstanceEnvironment server =
            ApplicationServer.getServerContext().getInstanceEnvironment();
        serverName_ = server.getName();

        initProperties();
    }

    private void initProperties() {

        try {
            
            // Check for property settings from domain.xml
            ServerContext sc = ApplicationServer.getServerContext();
            EjbContainer ejbc = ServerBeansFactory.
                getConfigBean(sc.getConfigContext()).getEjbContainer();
            EjbTimerService ejbt = ejbc.getEjbTimerService();

            if( ejbt != null ) {

                String valString = ejbt.getMinimumDeliveryIntervalInMillis();
                long val = (valString != null) ? 
                    Long.parseLong(valString) : -1;
                    
                if( val > 0 ) {
                    minimumDeliveryInterval_ = val;
                }

                valString = ejbt.getMaxRedeliveries();
                val = (valString != null) ? Long.parseLong(valString) : -1;
                // EJB 2.1 specification minimum is 1
                if( val > 0 ) {
                    maxRedeliveries_ = val;
                }

                valString = ejbt.getRedeliveryIntervalInternalInMillis();
                val = (valString != null) ? Long.parseLong(valString) : -1;
                if( val > 0 ) {
                    redeliveryInterval_ = val;
                }

                // If the system property com.sun.ejb.timer.ReadDBBeforeTimeout
                // is defined by the user use that the value of the flag
                // performDBReadBeforeTimeout 
                foundSysPropDBReadBeforeTimeout = 
                    getDBReadBeforeTimeoutProperty();

                // The default value for ReadDBBeforeTimeout in case of PE 
                // is false. For SE/EE the correct default would set when the 
                // EJBLifecyleImpl gets created as part of the EE lifecycle module
                setPerformDBReadBeforeTimeout( false );
            }

            // Compose owner id for all timers created with this 
            // server instance.  
            InstanceEnvironment server = sc.getInstanceEnvironment();
            String serverName = server.getName();
            ownerIdOfThisServer_ = serverName;

        } catch(Exception e) {
            logger.log(Level.FINE, "Exception converting timer service " +
               "domain.xml properties.  Defaults will be used instead.", e);
        }

        logger.log(Level.FINE, "EJB Timer Service properties : " +
                   "min delivery interval = " + minimumDeliveryInterval_ +
                   "\nmax redeliveries = " + maxRedeliveries_ +
                   "\nredelivery interval = " + redeliveryInterval_);
    }

    synchronized void timedObjectCount() {
        totalTimedObjectsInitialized_++;
    }

    /**
     * Return the ownerId of the server instance in
     * which we are running.
     */
    String getOwnerIdOfThisServer() {
        return ownerIdOfThisServer_;
    }

    /**
     *--------------------------------------------------------------
     * Methods to be implemented for DistributedEJBTimerService
     *--------------------------------------------------------------
     */

    /**
     * Create EJBException using the exception that is passed in
     */
    private EJBException createEJBException( Exception ex ) {
        EJBException ejbEx = new EJBException();
        ejbEx.initCause(ex);
        return ejbEx;
    }

    /**
     * Provide a count of timers owned by each server
     */
    public String[] listTimers( String[] serverIds ) {
        String[] totalTimers = new String[ serverIds.length ];
        try {
            for ( int i = 0; i < serverIds.length; i++ ) {
                totalTimers[i] = new String( 
                    new Integer(
                        timerLocalHome_.selectCountAllTimersOwnedBy( 
                                serverIds[i] )).toString());
            }
        } catch( Exception ex ) {
            logger.log( Level.SEVERE, "Exception in listTimers() : " , ex );

            //Propogate any exceptions caught
            EJBException ejbEx = createEJBException( ex );
            throw ejbEx;
        }
        return totalTimers;
    }

    /**
     * Take ownership of another server's timers.  
     */
    public int migrateTimers(String fromOwnerId) {

        String ownerIdOfThisServer = getOwnerIdOfThisServer();

        if( fromOwnerId.equals(ownerIdOfThisServer) ) {
            /// Error. The server from which timers are being
            // migrated should never be up and running OR receive this
            // notification.
            logger.log(Level.WARNING, "Attempt to migrate timers from " +
                        "an active server instance " + ownerIdOfThisServer);
            throw new IllegalStateException("Attempt to migrate timers from " +
                                            " an active server instance " + 
                                            ownerIdOfThisServer);
        }

        logger.log(Level.INFO, "Beginning timer migration process from " +
                   "owner " + fromOwnerId + " to " + ownerIdOfThisServer);

        TransactionManager tm = Switch.getSwitch().getTransactionManager();

        Set toRestore = new HashSet();

        try {
                              
            tm.begin();

            // The timer objects we'll use to set the new owner id come
            // from TimerMigrationBean
            Set toMigrate = timerMigrationLocalHome_.
                selectAllTimersOwnedBy(fromOwnerId);

            // The timer objects we'll use for restoring come from TimerBean
            // The same EJB QL query is used for selectAllTimersOwnedBy(owner)
            toRestore = timerLocalHome_.
                selectAllTimersOwnedBy(fromOwnerId);

            for(Iterator iter = toMigrate.iterator(); iter.hasNext();) {
                TimerMigrationLocal next  = (TimerMigrationLocal) iter.next();
                next.setOwnerId(ownerIdOfThisServer);
            }

            tm.commit();

        } catch(Exception e) {
            // Don't attempt to restore any timers since an error has
            // occurred.  This could be the expected result in the case that
            // multiple server instances attempted the migration at the same
            // time.  
            //FindBugs [Deadstore]: toRestore = new HashSet();

            logger.log(Level.FINE, "timer migration error", e);

            try {
                tm.rollback();
            } catch(Exception re) {
                logger.log(Level.FINE, "timer migration rollback error", re);
            }

            //Propagate the exception caught 
            EJBException ejbEx = createEJBException( e );
            throw ejbEx;
        }

	int totalTimersMigrated = toRestore.size();

        if( toRestore.size() > 0 ) {

            boolean success = false;
            try {
                
                logger.log(Level.INFO, "Timer migration phase 1 complete. " +
                           "Changed ownership of " + toRestore.size() + 
                           " timers.  Now reactivating timers...");

                tm.begin();
                
                _restoreTimers(toRestore);    
                success = true;
                
            } catch(Exception e) {

                logger.log(Level.FINE, "timer restoration error", e);

                //Propogate any exceptions caught as part of the transaction 
                EJBException ejbEx = createEJBException( e );
                throw ejbEx;

            } finally {
                // We're not modifying any state in this tx so no harm in
                // always committing.
                try {
                    tm.commit();
                } catch(Exception re) {
                    logger.log(Level.FINE, "timer migration error", re);   

                    if( success ) {
                        //Propogate any exceptions caught when trying to commit
                        //the transaction
                        EJBException ejbEx = createEJBException( re );
                        throw ejbEx;
                    }
                }
            }
        } else {
            logger.log(Level.INFO, fromOwnerId + " has 0 timers in need " +
                       "of migration");                    
        }
        
        return totalTimersMigrated;

    } //migrateTimers()

    public void setPerformDBReadBeforeTimeout( boolean defaultDBReadValue ) {

        // If the system property com.sun.ejb.timer.ReadDBBeforeTimeout
        // has been defined by the user then use that value else use the default
        if ( !foundSysPropDBReadBeforeTimeout ) {
            performDBReadBeforeTimeout = defaultDBReadValue;

            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "EJB Timer Service property : " +
                           "\nread DB before timeout delivery = " +  
                           performDBReadBeforeTimeout);
            }

        }
    }

    /**
     * Check to see if the user has defined a System property to specify if 
     * we need to check the timer table in the database and confirm that the 
     * timer is valid before delivering the ejbTimeout() for that timer.
     * 
     * In case of PE - the default value is false
     * and for SE/EE - the default value is true
     * 
     * But in all cases (PE/SE/EE) the user can set the System property 
     * "READ_DB_BEFORE_EJBTIMEOUT" to change the behaviour
     */
    private boolean getDBReadBeforeTimeoutProperty() {

        boolean result = false;
        try{
            Properties props = System.getProperties();
            String str=props.getProperty( strDBReadBeforeTimeout );
            if( null != str) {
		str = str.toLowerCase();
                performDBReadBeforeTimeout = Boolean.valueOf(str).booleanValue();

                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, "EJB Timer Service property : " +
                               "\nread DB before timeout delivery = " +  
                               performDBReadBeforeTimeout);
                }

                result = true;
            }
        } catch(Exception e) {
            logger.log(Level.INFO,
                "ContainerFactoryImpl.getDebugMonitoringDetails(), " +
                " Exception when trying to " + 
                "get the System properties - ", e);
        }
        return result;
    }

    /**
     * Called at server startup *after* user apps have been re-activated
     * to restart any active EJB timers.  
     */
    void restoreTimers() throws Exception {

        // Optimization.  Skip timer restoration if there aren't any
        // applications with timed objects deployed.  
        if( totalTimedObjectsInitialized_ == 0 ) {
            return;
        }

        TransactionManager tm = Switch.getSwitch().getTransactionManager();
        Set allActiveTimers = new HashSet();
        try {
            // create a tx in which to do database access for all timers 
            // needing restoration.  This gives us better performance that 
            // doing individual transactions per timer.            
            tm.begin();

            // This operation can take a while, since in some configurations
            // this will be the first time the connection to the database
            // is initialized.  In addition, there's an initialization 
            // cost to generating the SQL for the underlying
            // ejbql queries the first time any TimerBean query is called.
            allActiveTimers = 
                timerLocalHome_.selectAllActiveTimersOwnedByThisServer();

            _restoreTimers(allActiveTimers);
        } catch(Exception e) {

            // Problem accessing timer service so disable it.
            ContainerFactoryImpl cf = (ContainerFactoryImpl)
                Switch.getSwitch().getContainerFactory();
            cf.setEJBTimerService(null);

            logger.log(Level.WARNING, "ejb.timer_service_init_error", e);

            // No need to propagate exception.  EJB Timer Service is disabled
            // but that won't affect the rest of the EJB container services.
            return;

        } finally {
            // try to commit regardless of success or failure. 
            try {
                tm.commit();
            } catch(Exception e) {
                logger.log(Level.WARNING, "ejb.timer_service_init_error", e);
            }
        }
    }

    /**
     * The portion of timer restoration that deals with registering the
     * JDK timer tasks and checking for missed expirations.
     */
    private void _restoreTimers(Set timersEligibleForRestoration) {
                      
        // Do timer restoration in two passes.  The first pass updates
        // the timer cache with each timer.  The second pass schedules
        // the JDK timer tasks.  
        
        Map timersToRestore = new HashMap();

        for(Iterator iter = timersEligibleForRestoration.iterator(); 
            iter.hasNext();) {

            TimerLocal next  = (TimerLocal) iter.next();
            long containerId = next.getContainerId();

            // Timer might refer to an obsolete container.
            BaseContainer container = getContainer(containerId);
            if( container != null ) {
                
                TimerPrimaryKey timerId = (TimerPrimaryKey)next.getPrimaryKey();
                Date initialExpiration = next.getInitialExpiration();
               
                // Create an instance of RuntimeTimerState. 

                // Only access timedObjectPrimaryKey if timed object is
                // an entity bean.  That allows us to lazily load the underlying
                // blob for stateless session and message-driven bean timers.
                Object timedObjectPrimaryKey = null;
                if( container instanceof EntityContainer ) {
                    timedObjectPrimaryKey = next.getTimedObjectPrimaryKey();
                }

                RuntimeTimerState timerState = new RuntimeTimerState
                    (timerId, initialExpiration,
                     next.getIntervalDuration(), container, 
                     timedObjectPrimaryKey);                   

                timerCache_.addTimer(timerId, timerState);
                
                // If a single-action timer is still in the database it never
                // successfully delivered, so always reschedule a timer task 
                // for it.  For periodic timers, we use the last known
                // expiration time to decide whether we need to fire one
                // ejbTimeout to make up for any missed ones.
                Date expirationTime = initialExpiration;
                Date now = new Date();

                if( timerState.isPeriodic() ) {
                    // lastExpiration time, or null if we either aren't
                    // tracking last expiration or an expiration hasn't
                    // occurred yet for this timer.
                    Date lastExpiration = next.getLastExpiration();

                    // @@@ need to handle case where last expiration time
                    // is not stored in database.  This will be the case
                    // when we add configuration for update-db-on-delivery.
                    // However, for now assume we do update the db on each
                    // ejbTimeout.  Therefore, if (lastExpirationTime == null),
                    // it means the timer didn't successfully complete any
                    // timer expirations.                  
                                                     
                    if( (lastExpiration == null) &&
                        now.after(initialExpiration) ) {
                        
                        // This timer didn't even expire one time.
                        logger.log(Level.INFO, 
                                   "Rescheduling missed expiration for " +
                                   "periodic timer " +
                                   timerState + ". Timer expirations should " +
                                   " have been delivered starting at " +
                                   initialExpiration);

                        // keep expiration time at initialExpiration.  That
                        // will force an ejbTimeout almost immediately. After
                        // that the timer will return to fixed rate expiration.
                                                
                    } else if ( (lastExpiration != null) &&
                                ( (now.getTime() - lastExpiration.getTime()
                                   > next.getIntervalDuration()) ) ) {
                        
                        logger.log(Level.INFO, 
                                   "Rescheduling missed expiration for " +
                                   "periodic timer " +
                                   timerState + ".  Last timer expiration " +
                                   "occurred at " + lastExpiration);
                        
                        // Timer expired at least once and at least one
                        // missed expiration has occurred.

                        // keep expiration time at initialExpiration.  That
                        // will force an ejbTimeout almost immediately. After
                        // that the timer will return to fixed rate expiration.
                        
                    } else {

                        // In this case, at least one expiration has occurred
                        // but that was less than one period ago so there were
                        // no missed expirations.                     
                        expirationTime = 
                            calcNextFixedRateExpiration(timerState);
                    }
                    
                } else {  // single-action timer

                    if( now.after(initialExpiration) ) {
                        logger.log(Level.INFO, 
                                   "Rescheduling missed expiration for " +
                                   "single-action timer " +
                                   timerState + ". Timer expiration should " +
                                   " have been delivered at " +
                                   initialExpiration);
                    }
                }

                timersToRestore.put(timerState, expirationTime);

            } else {
                // Timed object's container no longer exists.
                try {
                    next.remove();
                } catch(RemoveException e) {
                    logger.log(Level.FINE, 
                        "Removing timer " + next.getPrimaryKey() + 
                               " for unknown container " + containerId, e);
                }
            }
        } // End -- for each active timer

        for(Iterator entries = timersToRestore.entrySet().iterator(); 
            entries.hasNext(); ) {
            Map.Entry next  = (Map.Entry) entries.next();
            RuntimeTimerState nextTimer = (RuntimeTimerState) next.getKey();
            TimerPrimaryKey timerId    = nextTimer.getTimerId();
            Date expiration = (Date) next.getValue();
            scheduleTask(timerId, expiration);
            logger.log(Level.FINE, 
                       "EJBTimerService.restoreTimers(), scheduling timer " + 
                       nextTimer);
        }

        logger.log(Level.FINE, "DONE EJBTimerService.restoreTimers()");
    }

    void shutdown() {
        // Set flag to prevent any new timer expirations.
        shutdown_ = true;
    }

    /**
     * Cancel all timers associated with a particular entity bean 
     * identity.  This is typically called when an entity bean is 
     * removed.  Note that this action falls under the normal EJB 
     * Timer removal semantics, which means it can be rolled back 
     * if the transaction rolls back.
     */
    void cancelEntityBeanTimers(long containerId, Object primaryKey) {
        try {
            // Get *all* timers for this entity bean identity.  This includes
            // even timers *not* owned by this server instance, but that 
            // are associated with the same entity bean and primary key.
            Collection timers = getTimers(containerId, primaryKey);
            if( logger.isLoggable(Level.FINE) ) {
                if( timers.isEmpty() ) {
                    logger.log(Level.FINE, "0 cancelEntityBeanTimers for " +
                               containerId + ", " + primaryKey);
                }
            }
            for(Iterator iter = timers.iterator(); iter.hasNext();) {
                TimerLocal next = (TimerLocal) iter.next();
                try {
                    cancelTimer(next);
                } catch(Exception e) {
                    logger.log(Level.WARNING, "ejb.cancel_entity_timer",
                               new Object[] { next.getPrimaryKey() });
                    logger.log(Level.WARNING, "", e);
                }
            }
        } catch(Exception e) {
            logger.log(Level.WARNING, "ejb.cancel_entity_timers",
                       new Object[] { new Long(containerId), primaryKey });
            logger.log(Level.WARNING, "", e);
        }
    }

    /**
     * Destroy all timers associated with a particular ejb container
     * and owned by this server instance.  
     * This is typically called when an ejb is undeployed.  It expunges
     * all timers whose timed object matches the given container.  In
     * the case of an entity bean container, all timers associated with
     * any of that container's entity bean identities will be destroyed.
     * This action *can not* be rolled back.  
     */
    void destroyTimers(long containerId) {
        Set timers = null;

        TransactionManager tm = Switch.getSwitch().getTransactionManager();

        try {
            
            // create a tx in which to do database access for all timers 
            // that need to be deleted.  This gives us better performance that 
            // doing individual transactions per timer.            
            tm.begin();
            
            // Get *all* timers for this ejb. Since the app is being undeployed
            // any server instance can delete all the timers for the same ejb.
            // Whichever one gets there first will actually do the delete. 
            timers = timerLocalHome_.selectTimersByContainer(containerId);

            for(Iterator iter = timers.iterator(); iter.hasNext();) {
                TimerLocal next = (TimerLocal) iter.next();
                TimerPrimaryKey nextTimerId = null;
                RuntimeTimerState nextTimerState = null;
                try {
                    nextTimerId = (TimerPrimaryKey) next.getPrimaryKey();
                    nextTimerState = getTimerState(nextTimerId);
                    if( nextTimerState != null ) {
                        synchronized(nextTimerState) {
                            if( nextTimerState.isScheduled() ) {
                                EJBTimerTask timerTask = 
                                    nextTimerState.getCurrentTimerTask();
                                timerTask.cancel();
                            } 
                        }
                    }
                    next.remove();
                } catch(Exception e) {
                    logger.log(Level.WARNING, "ejb.destroy_timer_error",
                               new Object[] { nextTimerId });
                    logger.log(Level.WARNING, "", e);
                } finally {
                    if( nextTimerState != null ) {
                        timerCache_.removeTimer(nextTimerId);
                    }
                }
            }

        } catch(Exception ex) {
            logger.log(Level.WARNING, "destroy_timers_error",
                       new Object[] { new Long(containerId) });
            logger.log(Level.WARNING, "", ex);
            return;
        } finally {
            try {
                tm.commit();
            } catch(Exception e) {
                // Most likely caused by two or more server instances trying
                // to delete the timers for the same ejb at the same time.
                logger.log(Level.WARNING, "destroy_timers_error",
                           new Object[] { new Long(containerId) });
                logger.log(Level.WARNING, "", e);
            }
        }

        return;
    }

    void rescheduleTask(TimerPrimaryKey timerId, Date expiration) {
        scheduleTask(timerId, expiration, true);
    }

    void scheduleTask(TimerPrimaryKey timerId, Date expiration) {
        scheduleTask(timerId, expiration, false);
    }

    void scheduleTask(TimerPrimaryKey timerId, Date expiration, 
                      boolean rescheduled) {
    
        RuntimeTimerState timerState = getTimerState(timerId);

        if( timerState != null ) {
            synchronized(timerState) {

                Date timerExpiration = expiration;

                if( !rescheduled ) {
                    // Guard against very small timer intervals. The EJB Timer 
                    // service is defined in units of milliseconds, but it is
                    // intended for coarse-grained events.  Very small timer
                    // intervals (e.g. 1 millisecond) are likely to overload 
                    // the server, so compensate by adjusting to a configurable
                    // minimum interval. 
                    Date cutoff = new Date(new Date().getTime() + 
                                           getMinimumDeliveryInterval());
                    if( expiration.before(cutoff) ) {
                        timerExpiration = cutoff;
                    }
                }

                EJBTimerTask timerTask = 
                    new EJBTimerTask(timerExpiration, timerId, this);
                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, (rescheduled ? "RE-" : "") + 
                               "Scheduling " + timerState + 
                               " for timeout at " + timerExpiration);
                }
                if( rescheduled ) {
                    timerState.rescheduled(timerTask);
                } else {
                    timerState.scheduled(timerTask);
                }

                java.util.Timer jdkTimer = Switch.getSwitch().getTimer();
                jdkTimer.schedule(timerTask, timerExpiration);            
            }
        } else {
            
            logger.log(Level.FINE, "No timer state found for " +
                       (rescheduled ? "RE-schedule" : "schedule") +
                       " request of " + timerId + 
                       " for timeout at " + expiration);            
        }
    }


    /**
     * Called from TimerBean to cancel the next scheduled expiration 
     * for a timer.
     * @return (initialExpiration time) if state is CREATED or
     *         (time that expiration would have occurred) if state=SCHEDULED or
     *         (null) if state is BEING_DELIVERED or timer id not found
     */
    Date cancelTask(TimerPrimaryKey timerId) {
       
        Date timeout = null;

        RuntimeTimerState timerState = getTimerState(timerId);
        if( timerState != null ) {
            synchronized(timerState) {

                if( timerState.isCreated() ) {
                    timeout = timerState.getInitialExpiration();
                } else if( timerState.isScheduled() ) {
                    EJBTimerTask timerTask = timerState.getCurrentTimerTask();
                    timeout = timerTask.getTimeout();
                    timerTask.cancel();
                }
                timerState.cancelled();

            }
        } else {
            logger.log(Level.FINE, "No timer state found for " +
                       "cancelTask request of " + timerId);                   
        }

        return timeout;
    }

    /**
     * Called from TimerBean in case where Timer is cancelled from within
     * its own ejbTimeout method and then rolled back.
     */
    void restoreTaskToDelivered(TimerPrimaryKey timerId) {
        
        RuntimeTimerState timerState = getTimerState(timerId);
        if( timerState != null ) {
            synchronized(timerState) {
                timerState.restoredToDelivered();
            }
            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "Restoring " + timerId + 
                   " to delivered state after it was cancelled and " +
                   " rolled back from within its own ejbTimeout method");
            }
        } else {
            logger.log(Level.FINE, "No timer state found for " +
                       "restoreTaskToDelivered request of " + timerId);       
        }
    }

    void expungeTimer(TimerPrimaryKey timerId) {
        // Expunge timer from timer cache without removing timer associated
        // timer bean.
        expungeTimer(timerId, false);
    }


    private Date calcInitialFixedRateExpiration(long timerServiceWentDownAt,
            RuntimeTimerState timerState)
    {
        if (!timerState.isPeriodic()) {
            throw new IllegalStateException();
        }
        Date now = new Date();

        long nowMillis = now.getTime();
        long initialExpiration = timerState.getInitialExpiration().getTime();

        long now2initialDiff = nowMillis - initialExpiration;
        long count = now2initialDiff / timerState.getIntervalDuration();
        long previousExpiration = 
            initialExpiration  + (count * timerState.getIntervalDuration());

        if ((previousExpiration >= timerServiceWentDownAt)
                && (previousExpiration <= nowMillis))
        {
            //We certainly missed this one while the server was down
            logger.log(Level.INFO, "ejb.deliver_missed_timer",
                       new Object[] { timerState.getTimerId(),
                                      new Date(previousExpiration) });
            return now;
        } else {
            //Calculate the new expiration time
            return calcNextFixedRateExpiration(timerState);
        }

    }

    private Date calcNextFixedRateExpiration(RuntimeTimerState timerState) {

        if( !timerState.isPeriodic() ) {
            throw new IllegalStateException("Timer " + timerState + " is " +
                                            "not a periodic timer");
        }

        Date initialExpiration = timerState.getInitialExpiration();
        long intervalDuration  = timerState.getIntervalDuration();

        return calcNextFixedRateExpiration(initialExpiration, intervalDuration);
    }
    
    private Date calcNextFixedRateExpiration(Date initialExpiration,
                                             long intervalDuration) {       

        Date now = new Date();
        long nowMillis = now.getTime();

        // In simplest case, initial expiration hasn't even occurred yet.
        Date nextExpirationTime = initialExpiration;
        
        if( now.after(initialExpiration) ) {
            long timeSinceInitialExpire = 
                (nowMillis - initialExpiration.getTime());
                                
            // number of time intervals since initial expiration.
            // intervalDuration is guaranteed to be >0 since this is a 
            // periodic timer.
            long numIntervals = 
                (timeSinceInitialExpire / intervalDuration);
            
            // Increment the number of intervals and multiply by the interval 
            // duration to calculate the next fixed-rate boundary.
            nextExpirationTime = new Date(initialExpiration.getTime() +
                ((numIntervals + 1) * intervalDuration));
        }

        return nextExpirationTime;
    }

    /**
     * Remove all traces of a timer.  This should be written defensively
     * so that if expunge is called multiple times for the same timer id,
     * the second, third, fourth, etc. calls will not cause exceptions.
     */
    private void expungeTimer(TimerPrimaryKey timerId, 
                              boolean removeTimerBean) {
        // First remove timer bean.  Don't update cache until
        // afterwards, since accessing of timer bean might require
        // access to timer state(e.g. timer application classloader)
        if( removeTimerBean ) {
            removeTimerBean(timerId);
        }
        timerCache_.removeTimer(timerId);
    }

    /**
     * @param primaryKey can be null if timed object is not an entity bean.
     * @return Primary key of newly created timer
     */
    TimerPrimaryKey createTimer(long containerId, Object timedObjectPrimaryKey,
                                long initialDuration, long intervalDuration, 
                                Serializable info) throws CreateException {

        Date now = new Date();

        Date initialExpiration = new Date(now.getTime() + initialDuration);

        return createTimer(containerId, timedObjectPrimaryKey, 
                           initialExpiration, intervalDuration, info);
    }

    /**
     * @param primaryKey can be null if timed object is not an entity bean.
     * @return Primary key of newly created timer
     */
    TimerPrimaryKey createTimer(long containerId, Object timedObjectPrimaryKey,
                                Date initialExpiration, long intervalDuration,
                                Serializable info) throws CreateException {

        BaseContainer container = getContainer(containerId);
        if( container == null ) {
            throw new CreateException("invalid container id " + containerId +
                                      " in createTimer request");
        }
        
        Class ejbClass = container.getEJBClass();
        if( !container.isTimedObject() ) {
            throw new CreateException
                ("Attempt to create an EJB Timer from a bean that is " +
                 "not a Timed Object.  EJB class " + ejbClass + 
                 " must implement javax.ejb.TimedObject or " +
                 " annotation a timeout method with @Timeout");
        }

        TimerPrimaryKey timerId = new TimerPrimaryKey(getNextTimerId());

        RuntimeTimerState timerState = 
            new RuntimeTimerState(timerId, initialExpiration, 
                                  intervalDuration, container, 
                                  timedObjectPrimaryKey);

        synchronized(timerState) {
            // Add timer entry before calling TimerBean.create, since 
            // create() actions might call back on EJBTimerService and 
            // need access to timer cache.
            timerCache_.addTimer(timerId, timerState);
            try {
                timerLocalHome_.create(timerId.getTimerId(), containerId, 
                                       ownerIdOfThisServer_,
                                       timedObjectPrimaryKey, 
                                       initialExpiration, intervalDuration, 
                                       info);
            } catch(Exception e) {
                logger.log(Level.SEVERE, "ejb.create_timer_failure",
                           new Object[] { new Long(containerId), 
                                          timedObjectPrimaryKey,
                                          info });
                logger.log(Level.SEVERE, "", e);
                // Since timer was never created, remove it from cache.
                timerCache_.removeTimer(timerId);
                if( e instanceof CreateException ) {
                    throw ((CreateException)e);
                } else {
                    EJBException ejbEx = new EJBException();
                    ejbEx.initCause(e);
                    throw ejbEx;
                }
            } 
        }

        return timerId;
    }

    /**
     * Use database query to retrieve all active timers.  Results must
     * be transactionally consistent. E.g.,  a client calling
     * getTimers within a transaction where a timer has been
     * created but not committed "sees" the timer but a client
     * in a different transaction doesn't. 
     *
     * @param primaryKey can be null if not entity bean
     *
     * @return Collection of TimerLocal objects.
     */
    private Collection getTimers(long containerId, 
                                 Object timedObjectPrimaryKey) 
        throws FinderException {

        // The results should include all timers for the given ejb
        // and/or primary key, including timers owned by other server instances.

        // @@@ Might want to consider cases where we can use 
        // timer cache to avoid some database access in PE/SE, or
        // even in EE with the appropriate consistency tradeoff.
        
        Collection activeTimers = 
            timerLocalHome_.selectActiveTimersByContainer(containerId); 
        
        Collection timersForTimedObject = activeTimers;

        if( timedObjectPrimaryKey != null ) { 
                                  
            // Database query itself can't do equality check on primary
            // key of timed object so perform check ourselves.
           
            timersForTimedObject = new HashSet();
            
            for(Iterator iter = activeTimers.iterator(); iter.hasNext();) {
                TimerLocal next = (TimerLocal) iter.next();
               
                Object nextTimedObjectPrimaryKey = 
                    next.getTimedObjectPrimaryKey();
                if( nextTimedObjectPrimaryKey.equals(timedObjectPrimaryKey) ) {
                    timersForTimedObject.add(next);
                }
            }
        }

        return timersForTimedObject;
    }

    /**
     * Use database query to retrieve the timer ids of all active 
     * timers.  Results must be transactionally consistent. E.g.,  
     * a client calling getTimerIds within a transaction where a 
     * timer has been created but not committed "sees" the timer 
     * but a client in a different transaction doesn't. Called by
     * EJBTimerServiceWrapper when caller calls getTimers.
     *
     * @param primaryKey can be null if not entity bean
     * @return Collection of Timer Ids.
     */
    Collection getTimerIds(long containerId, Object timedObjectPrimaryKey) 
        throws FinderException {

        // The results should include all timers for the given ejb
        // and/or primary key, including timers owned by other server instances.

        // @@@ Might want to consider cases where we can use 
        // timer cache to avoid some database access in PE/SE, or
        // even in EE with the appropriate consistency tradeoff.              
        
        Collection timerIdsForTimedObject = new HashSet();

        if( timedObjectPrimaryKey == null ) {

            timerIdsForTimedObject =
                timerLocalHome_.selectActiveTimerIdsByContainer(containerId);

        } else {
                                  
            // Database query itself can't do equality check on primary
            // key of timed object so perform check ourselves.
           
            Collection timersForTimedObject = getTimers(containerId, 
                                                        timedObjectPrimaryKey);

            timerIdsForTimedObject = new HashSet();
            
            for(Iterator iter = timersForTimedObject.iterator(); 
                iter.hasNext();) {
                TimerLocal next = (TimerLocal) iter.next();
                timerIdsForTimedObject.add(next.getPrimaryKey());
            }
        }

        return timerIdsForTimedObject;
    }
    
    /**
     * Get the application class loader for the timed object
     * that created a given timer.
     */
    ClassLoader getTimerClassLoader(long containerId) {       
        BaseContainer container = getContainer(containerId);        
        return (container != null) ? container.getClassLoader() : null;
    }

    TimerLocalHome getTimerBeanHome() {
        return timerLocalHome_;
    }

    private RuntimeTimerState getTimerState(TimerPrimaryKey timerId) {
        return timerCache_.getTimerState(timerId);
    }

    private TimerLocal findTimer(TimerPrimaryKey timerId) 
        throws FinderException {
        return timerLocalHome_.findByPrimaryKey(timerId);
    }       

    //
    // Logic used by TimerWrapper for javax.ejb.Timer methods.
    //

    void cancelTimer(TimerPrimaryKey timerId) 
        throws FinderException, Exception {

        // @@@ We can't assume this server instance owns the timer
        // so always ask the database.  Investigate possible use of
        // timer cache for optimization.

        // Look up timer bean from database.  Throws FinderException if
        // timer no longer exists.
        TimerLocal timerBean = findTimer(timerId);
        cancelTimer(timerBean);            
        
    }

    private void cancelTimer(TimerLocal timerBean) throws Exception {
        if( timerBean.isCancelled() ) {
            // Already cancelled within this tx.  Nothing more to do.
        } else {
            timerBean.cancel();
            timerBean.remove();
        }
    }

    /**
     * Return next planned timeout for this timer.  We have a fair amount
     * of leeway regarding the consistency of this information.  We should
     * strive to detect the case where the timer no longer exists.  However, 
     * since the current timer instance may not even own this timer, 
     * it's difficult to know the exact time of delivery in another server 
     * instance.  In the case of single-action timers, we return the 
     * expiration time that was provided upon timer creation.  For 
     * periodic timers, we can derive the next scheduled fixed rate
     * expiration based on the initial expiration and the interval.  
     */
    Date getNextTimeout(TimerPrimaryKey timerId) throws FinderException {

        // @@@ We can't assume this server instance owns the timer
        // so always ask the database.  Investigate possible use of
        // timer cache for optimization.

        TimerLocal timerBean = findTimer(timerId);
        if( timerBean.isCancelled() ) {
            // The timer has been cancelled within this tx.
            throw new FinderException("timer " + timerId + " does not exist");
        }

        Date initialExpiration = timerBean.getInitialExpiration();

        Date nextTimeout = timerBean.repeats() ? 
            calcNextFixedRateExpiration(initialExpiration, 
                                        timerBean.getIntervalDuration()) :
            initialExpiration;

        return nextTimeout;
    }

    Serializable getInfo(TimerPrimaryKey timerId) throws FinderException {
        
        // @@@ We can't assume this server instance owns the timer
        // so always ask the database.  Investigate possible use of
        // timer cache for optimization.

        TimerLocal timerBean = findTimer(timerId);
        if( timerBean.isCancelled() ) {
            // The timer has been cancelled within this tx.
            throw new FinderException("timer " + timerId + " does not exist");
        } 

        return timerBean.getInfo();
    }
    
    boolean timerExists(TimerPrimaryKey timerId) {
        boolean exists = false;

        // @@@ We can't assume this server instance owns the timer
        // so always ask the database.  Investigate possible use of
        // timer cache for optimization.
        
        try {
            TimerLocal timerBean = findTimer(timerId);
            // Make sure timer hasn't been cancelled within the current tx.
            exists = timerBean.isActive();
        } catch(FinderException fe) {
            exists = false;
        } 
        
        return exists;
    }

    private void removeTimerBean(TimerPrimaryKey timerId) {
        try {
            TimerLocal timerBean = findTimer(timerId);
            timerBean.remove();
        } catch(Throwable t) {
            logger.log(Level.WARNING, "ejb.remove_timer_failure",
                       new Object[] { timerId });
            logger.log(Level.WARNING, "", t);
        }
    }

    private BaseContainer getContainer(long containerId) {
        ContainerFactory cf = Switch.getSwitch().getContainerFactory();
        return (BaseContainer) cf.getContainer(containerId);
    }

    /**
     * Called from timer thread.  Used to deliver ejb timeout.
     */
    private void deliverTimeout(TimerPrimaryKey timerId) {

        if( logger.isLoggable(Level.FINE) ) {
            logger.log(Level.FINE, "EJBTimerService.deliverTimeout(): work " 
                       + 
                       "thread is processing work for timerId = " + timerId);
        }

        if( shutdown_ ) { 
            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "Cancelling timeout for " + timerId + 
                           " due to server shutdown.  Expiration " +
                           " will occur when server is restarted.");
            }
            return; 
        }
    
        RuntimeTimerState timerState = getTimerState(timerId);

        //
        // Make some defensive state checks.  It's possible that the
        // timer state changed between the time that the JDK timer task expired
        // and we got called on this thread.
        //

        if( timerState == null ) { 
            logger.log(Level.FINE, "Timer state is NULL for timer " + timerId +
                       " in deliverTimeout");
            return; 
        }

        BaseContainer container = getContainer(timerState.getContainerId());

        synchronized(timerState) {
            if( container == null ) {
                logger.log(Level.FINE, "Unknown container for timer " + 
                           timerId + " in deliverTimeout.  Expunging timer.");
                expungeTimer(timerId, true);
                return;
            } else if ( !timerState.isBeingDelivered() ) {
                logger.log(Level.FINE, "Timer state = " + 
                           timerState.stateToString() + 
                           "for timer " + timerId + " before callEJBTimeout");
                return;
            } else {
                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, "Calling ejbTimeout for timer " +
                               timerState);
                }
            }
        }
         
        try {
                    
            Switch.getSwitch().getCallFlowAgent().
                    requestStart(RequestType.TIMER_EJB);
            container.onEnteringContainer();
            // Need to address the case that another server instance 
            // cancelled this timer.  For maximum consistency, we will need 
            // to do a database read before each delivery.  This can have 
            // significant performance implications, so investigate possible 
            // reduced consistency tradeoffs.  
            if( performDBReadBeforeTimeout) {

                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, "For Timer :" + timerId + 
                    ": check the database to ensure that the timer is still " +
                    " valid, before delivering the ejbTimeout call" );
                }

                if( ! checkForTimerValidity(timerId) ) {
                    // The timer for which a ejbTimeout is about to be delivered
                    // is not present in the database. This could happen in the 
                    // SE/EE case as other server instances (other than the owner)
                    // could call a cancel on the timer - deleting the timer from 
                    // the database.
                    // Also it is possible that the timer is now owned by some other
                    // server instance
                    return;
                } 
            }

            ///
            // Call container to invoke ejbTimeout on the bean instance.
            // The remaining actions are divided up into two categories :
            //
            // 1) Actions that should be done within the same transaction
            //    context as the ejbTimeout call itself.  These are 
            //    handled by having the ejb container call back on the
            //    postEjbTimeout method after it has invoked bean.ejbTimeout
            //    but *before* it has called postInvoke.  That way any
            //    transactional operations like setting the last update time
            //    for periodic timers or removing a successfully delivered
            //    single-action timer can be done within the same tx as
            //    the ejbTimeout itself.  Note that there is no requirement
            //    that the ejbTimeout will be configured for CMT/RequiresNew.
            //    If there isn't a container-managed transaction,
            //    postEjbTimeout will still be called, and the database
            //    operations will be done in their own transaction.  While
            //    partitioning the actions like this adds some complexity,
            //    it's preferable to pushing this detailed timer semantics
            //    into the container's callEJBTimeout logic.
            //
            // 2) Post-processing for setting up next timer delivery and
            //    other redelivery conditions.  
            // 
            boolean redeliver = container.callEJBTimeout(timerState, this);

            if( shutdown_ ) {
                // Server is shutting down so we can't finish processing 
                // the timer expiration.  
                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, "Cancelling timeout for " + timerId
                               +
                               " due to server shutdown. Expiration will " +
                               " occur on server restart");
                }
                return;
            }

            // Resynchronize on timer state since a state change could have
            // happened either within ejbTimeout or somewhere else             

            timerState = getTimerState(timerId);

            if( timerState == null ) { 
                // This isn't an error case.  The most likely reason is that
                // the ejbTimeout method itself cancelled the timer.
                logger.log(Level.FINE, "Timer no longer exists for " + 
                           timerId + " after callEJBTimeout");
                return; 
            }


            synchronized(timerState) {
                Date now = new Date();
                if( timerState.isCancelled() ) {
                    // nothing more to do.
                } else if( redeliver ) {
                    if( timerState.getNumFailedDeliveries() <
                        getMaxRedeliveries() ) {
                        Date redeliveryTimeout = new Date
                            (now.getTime() + getRedeliveryInterval());
                        if( logger.isLoggable(Level.FINE) ) {
                            logger.log(Level.FINE,"Redelivering " + timerState);
                        }
                        rescheduleTask(timerId, redeliveryTimeout);
                    } else {
                        int numDeliv = timerState.getNumFailedDeliveries() + 1;
                        logger.log(Level.INFO, 
                           "ejb.timer_exceeded_max_deliveries",
                           new Object[] { timerState.toString(),
                                              new Integer(numDeliv)});
                        expungeTimer(timerId, true);
                    }
                } else if( timerState.isPeriodic() ) {

                    // Any necessary transactional operations would have
                    // been handled in postEjbTimeout callback.  Here, we
                    // just schedule the JDK timer task for the next ejbTimeout
                    
                    Date expiration = calcNextFixedRateExpiration(timerState);
                    scheduleTask(timerId, expiration);
                } else {
                   
                    // Any necessary transactional operations would have
                    // been handled in postEjbTimeout callback.  Nothing
                    // more to do for this single-action timer that was
                    // successfully delivered.                 
                }
            }

        } catch(Exception e) {
            logger.log(Level.FINE, "callEJBTimeout threw exception " +
                       "for timer id " + timerId , e);
            expungeTimer(timerId, true);
        } finally {
            container.onLeavingContainer();
            Switch.getSwitch().getCallFlowAgent().requestEnd();
        }
    }

    /**
     *  Called from BaseContainer during callEJBTimeout after bean.ejbTimeout
     *  but before postInvoke.  NOTE that this method is called whether or not
     *  the ejbTimeout method is configured for container-managed transactions.
     *  This method is *NOT* called if the container has already determined
     *  that a redelivery is necessary.
     *
     *  @return true if successful , false otherwise.  
     */
    boolean postEjbTimeout(TimerPrimaryKey timerId) {    

        boolean success = true;

        if( shutdown_ ) {
            // Server is shutting down so we can't finish processing 
            // the timer expiration.  
            return success;
        }

        // Resynchronize on timer state since a state change could have
        // happened either within ejbTimeout or somewhere else             

        RuntimeTimerState timerState = getTimerState(timerId);

        if( timerState != null ) { 
        
            // Since the ejbTimeout was called successfully increment the
            // delivery count
            BaseContainer container = getContainer(timerState.getContainerId());
            container.incrementDeliveredTimedObject();
                                  
            synchronized(timerState) {
                
                if( timerState.isCancelled() ) {
                    // nothing more to do. 
                } else {
                    
                    try {
                        TimerLocal timerBean = getValidTimerFromDB( timerId );
                        if( null == timerBean ) {
                            return false;
                        }

                        if( timerState.isPeriodic() ) {
                            Date now = new Date();
                            timerBean.setLastExpiration(now);   
                            
                            // Since timer was successfully delivered, update
                            // last delivery time in database if that option is
                            // enabled. 
                            // @@@ add configuration for update-db-on-delivery
                            if( logger.isLoggable(Level.FINE) ) {
                                logger.log(Level.FINE, 
                                           "Setting last expiration " +
                                           " for periodic timer " + timerState +
                                           " to " + now);
                            }                            

                        } else {
                                                        
                            if( logger.isLoggable(Level.FINE) ) {
                                logger.log(Level.FINE, "Single-action timer " + 
                                   timerState + " was successfully delivered. "
                                   + " Removing...");
                            }

                            // Timer has expired sucessfully, so remove it.
                            cancelTimer(timerBean);
                        }
                    } catch(Exception e) {
                        
                        // @@@ i18N
                        logger.log(Level.WARNING, "Error in post-ejbTimeout " +
                                   "timer processing for " + timerState, e);
                        success = false;
                    }                                       
                }
            }
        } 

        return success;
    }

    /**
     * This method is called to check if the timer is still valid.
     * In the SE/EE case the timer might be cancelled by any other 
     * server instance (other than the owner server instance)
     * that is part of the same cluster. Until we have a messaging 
     * system in place we would have to do a database query to
     * check if the timer is still valid.
     * Also check that the timer is owned by the current server instance
     *
     *  @return false if the timer record is not found in the database, 
     *          true  if the timer is still valid.
     */
    private boolean checkForTimerValidity(TimerPrimaryKey timerId) {

        boolean result = true;

        TimerLocal timerBean = getValidTimerFromDB( timerId );
        if( null == timerBean) {
            result = false;
        }

        return result;
    }

    private TimerLocal getValidTimerFromDB(TimerPrimaryKey timerId) {

        boolean result       = true;
        TimerLocal timerBean = null;

        try {

            timerBean = findTimer(timerId); 

            // There is a possibility that the same timer might be 
            // migrated across to a different server. Hence check 
            // that the ownerId of the timer record is the same as
            // the current server 
            if( ! ( timerBean.getOwnerId().equals(
                ownerIdOfThisServer_) ) ) {
                logger.log(Level.WARNING, 
                    "The timer (" + timerId + ") is not owned by " +
                    "server (" + ownerIdOfThisServer_ + ") that " + 
                    "initiated the ejbTimeout. This timer is now " +
                    "owned by (" + timerBean.getOwnerId() + "). \n" +
                    "Hence delete the timer from " + 
                    ownerIdOfThisServer_ + "'s cache.");

                result = false;
            } 

        } catch( FinderException fex ) {
            // The timer does not exist in the database 
            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "Timer :" + timerId + 
                    ": has been cancelled by another server instance. " + 
                    "Expunging the timer from " + ownerIdOfThisServer_ + 
                    "'s cache.");
            }

            result = false;

        } finally {
            if( !result ) {
                // The timer is either not present in the database or it is now
                // owned by some other server instance, hence remove the cache 
                //entry for the timer from the current server 
                expungeTimer(timerId, false);
                timerBean = null;
            } else {
                if( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, 
                        "The Timer :" + timerId + 
                        ": is a valid timer for the server (" +
                        ownerIdOfThisServer_ + ")");
                }
            }
        } 

        return timerBean;
    }

    /**
     * This method is called back from the EJBTimerTask object 
     * on the JDK Timer Thread.  Work performed in this callback 
     * should be short-lived, so do a little bookkeeping and then
     * launch a separate thread to invoke ejbTimeout, etc.
     */

    void taskExpired(TimerPrimaryKey timerId) {
        RuntimeTimerState timerState = getTimerState(timerId);

        if( timerState != null ) {
            synchronized(timerState) {
                if( timerState.isScheduled() ) {
                    timerState.delivered();

                    if( logger.isLoggable(Level.FINE) ) {
                        logger.log(Level.FINE, 
                           "Adding work pool task for timer " + timerId);
                    }

                    TaskExpiredWork work = new TaskExpiredWork(this, timerId);
                    com.sun.ejb.containers.util.ContainerWorkPool.addLast(work);
                } else {
                    logger.log(Level.FINE, "Timer " + timerId + 
                               " is not in scheduled state.  Current state = "
                               + timerState.stateToString());
                }
            }
        } else {
            logger.log(Level.FINE, "null timer state for timer id " + timerId);
        }

        return;
    }

    /**
     * Generate a unique key for the persistent timer object.
     * Key must be unique across server shutdown and startup, and
     * within all server instances sharing the same timer table.
     */
    private synchronized String getNextTimerId() {

        if( nextTimerIdCounter_ <= 0 ) {
            nextTimerIdMillis_  = System.currentTimeMillis();
            nextTimerIdCounter_ = 1;
        } else {
            nextTimerIdCounter_++;
        }

        // @@@ Add cluster ID

        return new String(nextTimerIdCounter_ +
                          TIMER_ID_SEP + nextTimerIdMillis_ +
                          TIMER_ID_SEP + serverName_ + 
                          TIMER_ID_SEP + domainName_);
    }

    //
    // Accessors for timer service properties.
    //
    private long getMinimumDeliveryInterval() {
        return minimumDeliveryInterval_;
    }

    private long getMaxRedeliveries() {
        return maxRedeliveries_;
    }

    private long getRedeliveryInterval() {
        return redeliveryInterval_;
    }

    // 
    // This is a global cache of timer data *only for timers owned by
    // this server instance*.  It is not transactionally
    // consistent.  Operations requiring those semantics should query
    // the database for TimerBean info.  Any timer for which there is an
    // active JDK timer task must be contained within this cache. 
    //
    // Note : this class supports concurrent access.
    //
    private class TimerCache {

        // Maps timer id to timer state.
        private Map timers_;

        // Map of timer information per container.
        //
        // For stateless session beans and message-driven beans, 
        // container id is mapped to a Long value representing the 
        // number of timers.
        // 
        //
        // For entity beans, container id is mapped to a list of
        // primary keys.  NOTE : This list can contain duplicate primary keys
        // in the case where the same entity bean identity has more
        // than one associated timer.
        
        private Map containerTimers_;

        public TimerCache() {
            // Create unsynchronized collections.  TimerCache will 
            // provide concurrency control.
            timers_ = new HashMap();
            containerTimers_ = new HashMap();
        }

        public synchronized void addTimer(TimerPrimaryKey timerId, 
                                          RuntimeTimerState timerState) {
            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "Adding timer " + timerState);
            }

            timers_.put(timerId, timerState);

            Long containerId = new Long(timerState.getContainerId());

            Object containerInfo = containerTimers_.get(containerId);

            if( timerState.timedObjectIsEntity() ) {
                Collection entityBeans;
                if( containerInfo == null ) {
                    // NOTE : This list *can* contain duplicates, since
                    // the same entity bean can be the timed object for 
                    // multiple timers.
                    entityBeans = new ArrayList();
                    containerTimers_.put(containerId, entityBeans);
                } else {
                    entityBeans = (Collection) containerInfo;
                }
                entityBeans.add(timerState.getTimedObjectPrimaryKey());
            } else {
                Long timerCount = (containerInfo == null) ? new Long(1) :
                    new Long(((Long) containerInfo).longValue() + 1);
                containerTimers_.put(containerId, timerCount);
            }

        }

        /**
         * Remove a timer from the cache. This should be coded 
         * defensively since it's possible it will be called multiple
         * times for the same timer.
         */
        public synchronized void removeTimer(TimerPrimaryKey timerId) {
            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "Removing timer " + timerId);
            }

            RuntimeTimerState timerState = (RuntimeTimerState) 
                timers_.remove(timerId);

            if( timerState == null) {
                return;
            }

            Long containerId = new Long(timerState.getContainerId());
            Object containerInfo = containerTimers_.get(containerId);
                
            if( containerInfo != null ) {
                if( timerState.timedObjectIsEntity() ) {
                    Collection entityBeans = (Collection) containerInfo;
                    if( entityBeans.size() == 1 ) {
                        // Only one left -- blow away the container.
                        containerTimers_.remove(containerId);
                    } else {
                        // Remove a single instance of this primary key
                        // from the list.  There could still be other
                        // instances of the same primary key.
                        entityBeans.remove
                            (timerState.getTimedObjectPrimaryKey());
                    }
                } else {
                    long timerCount = ((Long) containerInfo).longValue();
                    if( timerCount == 1 ) {
                        // Only one left -- blow away the container
                        containerTimers_.remove(containerId);
                    } else {
                        Long newCount = new Long(timerCount - 1);
                        containerTimers_.put(containerId, newCount);
                    }                         
                }
            }
        }

        public synchronized RuntimeTimerState getTimerState(TimerPrimaryKey 
                                                            timerId) {
            return (RuntimeTimerState) timers_.get(timerId);
        }

        // True if the given entity bean has any timers and false otherwise.
        public synchronized boolean entityBeanHasTimers(long containerId, 
                                                        Object pkey) {
            Object containerInfo = containerTimers_.get(new Long(containerId));
            return (containerInfo != null) ?
                ((Collection) containerInfo).contains(pkey) : false;
        }

        // True if the ejb represented by this container id has any timers
        // and false otherwise.  
        public synchronized boolean containerHasTimers(long containerId) {
            return containerTimers_.containsKey(new Long(containerId));
        }

        // Placeholder for logic to ensure timer cache consistency.
        public synchronized void validate() {
        }

    } //TimerCache{}

    private File getTimerServiceShutdownFile()
        throws Exception
    {
        File timerServiceShutdownDirectory;
        File timerServiceShutdownFile;

        InstanceEnvironment env = ApplicationServer.getServerContext().
                                    getInstanceEnvironment();
        AppsManager appsManager = new AppsManager(env, false);

        String j2eeAppPath = appsManager.getLocation(appID);
        timerServiceShutdownDirectory = new File(j2eeAppPath + File.separator);
        timerServiceShutdownDirectory.mkdirs();
        timerServiceShutdownFile = new File(j2eeAppPath + File.separator
                + TIMER_SERVICE_FILE);

        return timerServiceShutdownFile;
    }

    private long getTimerServiceDownAt() {
        long timerServiceWentDownAt = -1;
        try {
            File timerServiceShutdownFile  = getTimerServiceShutdownFile();

            if (timerServiceShutdownFile.exists()) {
                DateFormat dateFormat =  
                    new SimpleDateFormat(TIMER_SERVICE_DOWNTIME_FORMAT);
        
                FileReader fr = new FileReader(timerServiceShutdownFile);
                BufferedReader br = new BufferedReader(fr, 128);
                String line = br.readLine();

                Date myDate = dateFormat.parse(line);
                timerServiceWentDownAt = myDate.getTime();
                logger.log(Level.INFO, "ejb.timer_service_last_shutdown",
                           new Object[] { line });
            } else {
                logger.log(Level.WARNING, "ejb.timer_service_shutdown_unknown",
                           new Object[] { timerServiceShutdownFile });
            }
        } catch (Throwable th) {
            logger.log(Level.WARNING, "ejb.timer_service_shutdown_unknown",
                       new Object[] { "" });
            logger.log(Level.WARNING, "", th);
        }
        return timerServiceWentDownAt;
    }


    /**
     * Called from TimerBeanContainer
     */
    public void onShutdown() {
        try {
            DateFormat dateFormat =  
                new SimpleDateFormat(TIMER_SERVICE_DOWNTIME_FORMAT);
            String downTimeStr = dateFormat.format(new Date());

            File timerServiceShutdownFile  = getTimerServiceShutdownFile();
            FileWriter fw = new FileWriter(timerServiceShutdownFile);
            PrintWriter pw = new PrintWriter(fw);

            pw.println(downTimeStr);

            pw.flush();
            pw.close();
            fw.close();
            logger.log(Level.INFO, "ejb.timer_service_shutdown_msg",
                       new Object[] { downTimeStr });
        } catch (Throwable th) {
            logger.log(Level.WARNING, "ejb.timer_service_shutdown_unknown",
                       new Object[] { TIMER_SERVICE_FILE });
            logger.log(Level.WARNING, "", th);
        }
    }


    /**
     * This class gets a callback on a worker thread where the actual
     * ejbTimeout invocation will be made.  
     */
    private static class TaskExpiredWork
        implements com.sun.enterprise.util.threadpool.Servicable
    {
        private EJBTimerService timerService_;
        private TimerPrimaryKey timerId_;

        public TaskExpiredWork(EJBTimerService timerService, 
                               TimerPrimaryKey timerId) {
            timerService_ = timerService;
            timerId_ = timerId;
        }

        public void prolog() { }
        
        public void epilog() { }
        
        public void service() { run(); }
        
        public void run() {
            // Delegate to Timer Service.
            timerService_.deliverTimeout(timerId_);
        } 

    } // TaskExpiredWork

}
