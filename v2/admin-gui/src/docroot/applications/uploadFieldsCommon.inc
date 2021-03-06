
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
<!-- applications/uploadFieldsCommon.inc -->

<sun:propertySheetSection id="psec" label="$resource{i18n.common.General}">
<sun:property id="nameProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.ApplicationName}" visible="#{true}">
	<sun:textField id="appName" columns="$int{40}" text="#{appName}" styleClass="required" required="#{true}" >
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{appNameId});
            />
        </sun:textField>
</sun:property>

<sun:property id="cxp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.webApp.ContextRoot}" helpText="$resource{i18n.webApp.contextRootHelp}" visible="#{false}">
	<sun:textField id="ctx" columns="$int{40}" text="#{ctxtRoot}" >
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{contextRootId});
            />
        </sun:textField>
</sun:property>

<sun:property id="vsp" labelAlign="left" noWrap="#{true}" rendered="#{displayVS}" overlapLabel="#{false}" label="$resource{i18n.deploy.virtualServerLabel}" helpText="$resource{i18n.deploy.vsHelp}" visible="#{false}">
	<sun:listbox id="vs" 
		immediate="#{true}"
		items="#{availableVS}"
		multiple="#{true}"
		rows="$int{4}"
		selected="#{selectedVS}"
	>
        	<!beforeCreate
			getAllVSList(
				targetName="server"
				PE="true"
				serverList=>$attribute{vsList});
			getSunOptions(
				labels="$attribute{vsList}"
				values="$attribute{vsList}"
				options=>$pageSession{availableVS});
			getListElement(
				list="$attribute{vsList}"
				index="0"
				selectedIndex=>$pageSession{selectedVS});
		/>
	</sun:listbox>
</sun:property>
<sun:property id="enableProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.status}" visible="#{true}"  helpText="$resource{i18n.deploy.statusHelp}">
	<sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{enabled}" />  
</sun:property>
<sun:property id="wsp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.JavaWebStart}" visible="#{false}"  helpText="$resource{i18n.deploy.javaWebStartHelp}">
	<sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{jws}" />  
</sun:property>
<sun:property id="haProp" rendered="#{supportCluster}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Availability}" visible="#{false}">
	<sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{ha}" />  
</sun:property>
<sun:property id="verifierProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.run}" visible="#{true}"  helpText="$resource{i18n.deploy.runHelp}">
	<sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{verifier}" />  
</sun:property>
<sun:property id="precmplProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.precompile}" visible="#{false}" helpText="$resource{i18n.server.PrecompileHelp}">
	<sun:checkbox id="precompileJSP" label="$resource{i18n.common.Enabled}" selected="#{precompile}" />  
</sun:property>
<sun:property id="librariesProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Libraries}" helpText="$resource{i18n.deploy.libraryHelp}" visible="#{false}">
	<sun:textField id="library" columns="$int{40}" text="#{library}" />
</sun:property>
<sun:property id="threadpoolProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorModule.ThreadPoolID}" helpText="$resource{i18n.connectorModule.ThreadPoolIDHelp}" visible="#{false}">
	<sun:textField id="threadPool" columns="$int{40}" text="#{threadpool}" />
</sun:property>
<sun:property id="registryProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorModule.RegistryType}" visible="#{false}">
	<sun:dropDown id="registryType" selected="#{requestScope.registryType}" labels={"", "UDDI 3.0", "ebXML"} values={"", "com.sun.appserv.registry.uddi", "com.sun.appserv.registry.ebxml"} />
</sun:property>
<sun:property id="descriptionProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}" helpText="$resource{i18n.javaMail.descHelp}" visible="#{true}">
	<sun:textField id="description" columns="$int{40}" text="#{description}" />
</sun:property>
</sun:propertySheetSection>
<sun:propertySheetSection id="advancedSection" label="$resource{i18n.common.Advanced}" rendered="#{false}">
	<sun:property id="rmistubProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.rmis}" helpText="$resource{i18n.deploy.rmistubHelp}">
	<sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{rmis}" />  
	</sun:property>
</sun:propertySheetSection>
