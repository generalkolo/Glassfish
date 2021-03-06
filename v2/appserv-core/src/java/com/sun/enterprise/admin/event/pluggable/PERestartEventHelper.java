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
package com.sun.enterprise.admin.event.pluggable;

import com.sun.logging.LogDomains;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.event.RRPersistenceHelper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.event.ElementChangeHelper;


//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Restart event Helper - class providing support for informing
 * server(s) to set its(their) restart required state to true or false.
 *
 * @author: Satish Viswanatham
 */
public final class PERestartEventHelper implements RestartEventHelper{

    // i18n StringManager
    private static final Logger _logger     = 
                        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    
    public PERestartEventHelper() {
    }

    /**
     * Given a config change list, this method figures out if that list contains
     * any non dynamic recofigurable changes. If there is any change which can
     * not be applied dynamically, if sets the restart status on the appropriate
     * target servers.
     *
     * @param ctx        Config context of DAS 
     * @param list       config change list
     */
    public void setRestartRequiredForTarget(ConfigContext ctx, 
            ArrayList configChangeList ) {
   
        try {
            AdminService admServ = AdminService.getAdminService();

            if ((admServ != null) && (admServ.isDas())) {
                Set nonDynSet = ElementChangeHelper.
                    getXPathesForDynamicallyNotReconfigurableElements(
                    configChangeList);

                Iterator iter = nonDynSet.iterator();

                if (iter.hasNext()) {
                    RRPersistenceHelper rrHelper = new RRPersistenceHelper();
                    rrHelper.setRestartRequired(true);
                }
            }
        } catch (Throwable t) {
            _logger.log(Level.INFO, "event.exception_during_restart_reset",
                        t);
        }

    }


}
