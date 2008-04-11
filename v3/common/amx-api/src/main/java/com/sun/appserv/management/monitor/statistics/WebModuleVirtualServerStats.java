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

package com.sun.appserv.management.monitor.statistics;

import com.sun.appserv.management.j2ee.statistics.StringStatistic;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Stats;

/**
	<b>Enterprise Edition only; not supported in PE/Glassfish.</b>
	@see com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor
 */
public interface WebModuleVirtualServerStats extends Stats
{
    /**
     * Gets the total number of rejected sessions for the web
     * module associated with this WebModuleStats.
     *
     * <p>This is the number of sessions that were not created because the
     * maximum allowed number of sessions were active.
     *.
     * @return Total number of rejected sessions
     */
	public CountStatistic getRejectedSessionsTotal();
	
    /**
     * Gets the total number of expired sessions for the web
     * module associated with this WebModuleStats.
     *.
     * @return Total number of expired sessions
     */
	public CountStatistic getExpiredSessionsTotal();
	
    /**
     * Gets the maximum number of concurrently active sessions for the web
     * module associated with this WebModuleStats.
     *
     * @return Maximum number of concurrently active sessions
     */
	public CountStatistic getActiveSessionsHigh();
	
    /**
     * Gets the number of currently active sessions for the web
     * module associated with this WebModuleStats.
     *.
     * @return Number of currently active sessions
     */
	public CountStatistic getActiveSessionsCurrent();
	
    /**
     * Gets the total number of sessions that have been created for the web
     * module associated with this WebModuleStats.
     *.
     * @return Total number of sessions created
     */
	public CountStatistic getSessionsTotal();
	
    /**
     * Gets the number of JSPs that have been loaded in the web module
     * associated with this WebModuleStats.
     *.
     * @return Number of JSPs that have been loaded
     */
	public CountStatistic getJSPCount();
	

    /**
     * Gets the number of JSPs that have been reloaded in the web module
     * associated with this WebModuleStats
     *.
     * @return Number of JSPs that have been reloaded
     */
	public CountStatistic getJSPReloadCount();
	
    /**
     * Gets the number of errors that were triggered by JSP invocations.
     *.
     * @return Number of errors triggered by JSP invocations
     */
    public CountStatistic getJSPErrorCount();
    
    /**
     */
	public CountStatistic getPassivatedSessionsCurrent();
	
    /**
     * Gets the cumulative processing times of all servlets in the web module
     * associated with this WebModuleStats.
     *
     * @return Cumulative processing times of all servlets in the web module
     * associated with this WebModuleStats
     */
    public CountStatistic getServletProcessingTimes();
    
    /**
     * Returns comma-separated list of all sessions currently active in the web
     * module associated with this WebModuleStats.
     *
     * @return Comma-separated list of all sessions currently active in the
     * web module associated with this WebModuleStats
     */
    public StringStatistic getSessions();
}
