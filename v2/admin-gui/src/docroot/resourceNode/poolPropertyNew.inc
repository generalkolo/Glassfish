<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
-->
<!-- resourceNode/poolPropertyNew.inc -->

    <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.initPoolSize}" helpText="$resource{i18n.jdbcPool.initPoolSizeHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{wizardPool.SteadyPoolSize}" /> 
        <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/> 
    </sun:property>

    <sun:property id="maxProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.maxPoolSize}" helpText="$resource{i18n.jdbcPool.maxPoolSizeHelp}">
        <sun:textField id="ds2" styleClass="integer" columns="$int{10}" text="#{wizardPool.MaxPoolSize}" />
        <sun:staticText id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="resizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.poolResize}" helpText="$resource{i18n.jdbcPool.poolResizeHelp}">
        <sun:textField id="ds3" styleClass="integer" columns="$int{10}" text="#{wizardPool.PoolResizeQuantity}" /> 
        <sun:staticText id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="idleProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.idleTimeout}" helpText="$resource{i18n.jdbcPool.idleTimeoutHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{wizardPool.IdleTimeoutInSeconds}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.maxWait}" helpText="$resource{i18n.jdbcPool.maxWaitHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{wizardPool.MaxWaitTimeInMillis}" /> 
        <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>



