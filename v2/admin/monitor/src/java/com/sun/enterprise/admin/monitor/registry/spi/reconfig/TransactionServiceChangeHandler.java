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

/* TransactionServiceChangeHandler.java
 * $Id: TransactionServiceChangeHandler.java,v 1.3 2005/12/25 03:43:42 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:42 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi.reconfig;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.StatsHolder;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;

import com.sun.enterprise.admin.monitor.registry.spi.ValueListMap;
import com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 * Provides for dynamic reconfiguration of transaction subsystem.
 * This class decides the actions to take when there are
 * changes to the monitoring level through administrative interfaces.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
class TransactionServiceChangeHandler implements ChangeHandler {
	
	private final ChangeHandler		successor;
	private final ValueListMap		listeners;
	
	private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	
	TransactionServiceChangeHandler(ChangeHandler successor, ValueListMap listeners) {
		this.successor	= successor;
		this.listeners	= listeners;
	}
	
	public void handleChange(MonitoredObjectType t, MonitoringLevel from, MonitoringLevel to) {
		if (isTransactionServiceType(t)) {
			handleChange(from, to);
		}
		else {
			successor.handleChange(t, from, to);
		}
	}
	
	private boolean isTransactionServiceType(MonitoredObjectType t) {
		return ( t == MonitoredObjectType.TRANSACTION_SERVICE );
	}
	
	private void handleChange(MonitoringLevel from, MonitoringLevel to) {
		if (off2Low(from, to) || off2High(from, to)) {
			notifyListeners(from, to);
			registerMBeans();
		}
		if (low2Off(from, to) || high2Off(from, to)) {
			unregisterMBeans();
			notifyListeners(from, to);
		}
		if (low2High(from, to) || high2Low(from, to)) {
			//currently do nothing.
		}
	}
	
	private void notifyListeners(MonitoringLevel from, MonitoringLevel to) {
		logger.finer("DynamicReconfigurator: Now notifying the listeners for jta stats --- from = " + from.toString() + " to = " + to.toString());
		final Map l = (Map)listeners.get(MonitoredObjectType.TRANSACTION_SERVICE);
		if (l == null)
			return;
		final Iterator it = l.keySet().iterator();
		while (it.hasNext()) {
			final MonitoringLevelListener ml = (MonitoringLevelListener)it.next();
			ml.changeLevel(from, to, MonitoredObjectType.TRANSACTION_SERVICE);
		}
	}
	
	private void registerMBeans() {
		final MonitoringRegistrationHelper registryImpl	= 
			(MonitoringRegistrationHelper) MonitoringRegistrationHelper.getInstance();
		//note that the above refers to the actual implementation rather than interface.

		//registers MBeans pertaining to transaction service
		final Iterator iter = registryImpl.getTransactionServiceNodes().iterator();
		while (iter.hasNext()) {
			final StatsHolder c = (StatsHolder) iter.next();
			c.registerMBean();
			logger.finer("DynamicReconfigurator: Now Registering MBean for --- " + c.getName());
		}
	}
	
	private void unregisterMBeans() {
		final MonitoringRegistrationHelper registryImpl	= 
			(MonitoringRegistrationHelper) MonitoringRegistrationHelper.getInstance();
		//note that the above refers to the actual implementation rather than interface.

		final Iterator iter = registryImpl.getTransactionServiceNodes().iterator();
		while (iter.hasNext()) {
			final StatsHolder c = (StatsHolder) iter.next();
			c.unregisterMBean();
			logger.finer("DynamicReconfigurator: Now UnRegistering MBean for --- " + c.getName());
		}
	}

	private boolean off2Low(MonitoringLevel from, MonitoringLevel to) { 
		return ( from == MonitoringLevel.OFF && to == MonitoringLevel.LOW );
	}
	private boolean off2High(MonitoringLevel from, MonitoringLevel to) { 
		return ( from == MonitoringLevel.OFF && to == MonitoringLevel.HIGH );
	}
	private boolean low2Off(MonitoringLevel from, MonitoringLevel to) { 
		return ( from == MonitoringLevel.LOW && to == MonitoringLevel.OFF);
	}
	private boolean high2Off(MonitoringLevel from, MonitoringLevel to) { 
		return ( from == MonitoringLevel.HIGH && to == MonitoringLevel.OFF );
	}
	private boolean low2High(MonitoringLevel from, MonitoringLevel to) { 
		return ( from == MonitoringLevel.LOW && to == MonitoringLevel.HIGH);
	}
	private boolean high2Low(MonitoringLevel from, MonitoringLevel to) {
		return ( from == MonitoringLevel.HIGH && to == MonitoringLevel.LOW );
	}
}
