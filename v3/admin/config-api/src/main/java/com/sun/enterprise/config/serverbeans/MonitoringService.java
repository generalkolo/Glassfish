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



package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.monitoring.ContainerMonitoring;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.quality.ToDo;



/* @XmlType(name = "", propOrder = {
    "moduleMonitoringLevels",
    "property"
}) */

@Configured
public interface MonitoringService extends ConfigBeanProxy, Injectable, org.glassfish.api.admin.config.PropertyBag {

    /**
     * Gets the value of the moduleMonitoringLevels property.
     *
     * @return possible object is
     *         {@link ModuleMonitoringLevels }
     */
    @Element
    public ModuleMonitoringLevels getModuleMonitoringLevels();

    /**
     * Sets the value of the moduleMonitoringLevels property.
     *
     * @param value allowed object is
     *              {@link ModuleMonitoringLevels }
     */
    public void setModuleMonitoringLevels(ModuleMonitoringLevels value) throws PropertyVetoException;
    
    /**
     * Properties as per {@link org.glassfish.api.admin.config.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    /**
     * Gets the value of the mbean-enabled attribute.
     * This boolean attribute determines whether monitoring mbeans are enabled
     * or disabled. When disabled, all montioring activity will be disabled
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue="true")
    public String getMbeanEnabled();

    /**
     * Sets the value of the mbean-enabled attribute.
     *
     * @param value allowed object is a String
     * 
     */
    public void setMbeanEnabled(String value) throws PropertyVetoException;

    // TODO: Ref: Issue # 8706. Sreeni to work with GmBal team and provide a
    // final resolution on where the above mbean-enabled flag would reside.
    // With the addition of the new attribute below, the above attribute becomes
    // misplaced. The attribute monitoring-enabled actually enables/disables the
    // monitoring infrastructure (POJOs) while mbean-enabled enables/disables
    // one type of veneer viz the mbean-layer

    /**
     * Gets the value of the monitoring-enabled attribute.
     * This boolean attribute determines whether monitoring mebans are enabled
     * or disabled. When disabled, all montioring activity will be disabled
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue="true")
    public String getMonitoringEnabled();

    /**
     * Sets the value of the monitoring-enabled attribute.
     *
     * @param value allowed object is String
     */
    public void setMonitoringEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the dtrace-enabled attribute.
     *
     * @return present dtrace status
     */
    @Attribute(defaultValue="false")
    public String getDtraceEnabled();

    /**
     * Sets the value of the dtrace-enabled attribute.
     *
     * @param value allowed object is String
     */
    public void setDtraceEnabled(String value) throws PropertyVetoException;

    /**
     * Get the monitoring configuration for containers that used the default
     * ContainerMonitoring.
     *
     * @return list of container monitoring configurations (default)
     */
    List<ContainerMonitoring> getContainerMonitorings();

    /**
     * Get the monitoring configuration for other types of containers that used
     * custom monitoring configuration.
     *
     * @return  list of container monitoring configurations
     */
    @Element("*")
    List<ContainerMonitoring> getMonitoringItems();

    /**
     * Return the monitoring configuration for a container by the provided name,
     * assuming the named container used the default ContainerMonitoring to
     * express its monitoring configuration.
     *
     * @param name name of the container to return the configuration for
     * @return the container configuration or null if not found
     */
    @DuckTyped
    ContainerMonitoring getContainerMonitoring(String name);

    public class Duck {
        public static ContainerMonitoring getContainerMonitoring(MonitoringService ms, String name) {
            for (ContainerMonitoring cm : ms.getMonitoringItems()) {
                if (cm.getName().equals(name)) {
                    return cm;
                }
            }
            return null;
        }
    }
}
