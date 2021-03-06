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
<!-- uploadComponent.inc -->

<sun:propertySheet id="sheet1">			 
    <sun:propertySheetSection id="section1" >
        <sun:property id="type" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.TypeLabel}"  rendered="#{chooseType}" > 
            <sun:dropDown id="appType" selected="#{appType}" 
                labels={"$resource{i18n.deploy.war}", "$resource{i18n.deploy.ear}", "$resource{i18n.deploy.rar}", "$resource{i18n.deploy.ejb}", "$resource{i18n.deploy.appClient}"} 
                values={"webApp", "application", "connector", "ejbModule", "appclient"} 
                onChange="javascript: setVisible(this.value)" >
                <!afterCreate
                    if ( $pageSession{chooseType}){
                        getClientId(component="$this{component}" clientId=>$page{typeId});
                    }
                />
            </sun:dropDown>
        </sun:property>

        <sun:property id="prop1" labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.location}">                           
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{locationPropId});
            />
            
            <sun:radioButton id="uploadRdBtn" name="uploadRdBtn"  
                label="$resource{i18n.deploy.chooseJarLabel}" 
                selected="#{uploadRdBtn}"  selectedValue="client" 
                onClick="javascript:
                    disableComponent('#{dirPathId}', 'text');
                    disableBtnComponent('#{dirSelectBtnId}');
                    disableBtnComponent('#{filSelectBtnId}');
                    enableComponent('#{fileuploadId}', 'file'); "
                    >
                <!beforeCreate 
                    setPageSessionAttribute(key="uploadRdBtn", value="client");
                />
           </sun:radioButton>
            "<br />                                        
            <sun:upload id="fileupload" style="margin-left: 17pt" columns="$int{50}"
                uploadedFile="#{requestScope.uploadedFile}" 
                onChange="javascript: setFieldValue('#{appNameId}', this.value, '#{typeId}', '#{contextRootId}', '#{extensionId}' ); "
                >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{fileuploadId});
                />
            </sun:upload>
            
            "<br /><br/><br/>
            <sun:radioButton id="fileChooseRdBtn" name="uploadRdBtn"
                selected="#{uploadRdBtn}"  selectedValue="serverSide" 
                label="$resource{i18n.deploy.chooseLocal}" 
                onClick="javascript:  
                    enableComponent('#{dirPathId}', 'text');
                    enableBtnComponent('#{dirSelectBtnId}');
                    enableBtnComponent('#{filSelectBtnId}');
                    disableComponent('#{fileuploadId}', 'file'); "
            /> 
            "<br />

            <sun:textField id="dirPath" required="#{false}" text="#{dirPath}" style="margin-left: 17pt" columns="$int{50}" 
                onChange="javascript: setFieldValue('#{appNameId}', this.value, '#{typeId}', '#{contextRootId}', '#{extensionId}' );" 
               >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{dirPathId});
                />
            </sun:textField>                                    
            
            <sun:button id="filSelectBtn" 
		 text="$resource{i18n.button.BrowseFile}" 
		 disabled="#{true}"
                 primary="#{false}" 
		 onClick="javascript: var win = window.open('../applications/fileChooser.jsf?dirPathId=#{dirPathId}&appNameId=#{appNameId}&ctxRootId=#{contextRootId}&typeId=#{typeId}&extensionId=#{extensionId}', 'fileChooserPopup', 'height=580, width=650, top='+((screen.height - (screen.height/1.618))-(500/2))+',left='+((screen.width-650)/2)+', scrollbars, resizable');win.focus(); return false" >
                 <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{filSelectBtnId});
                />
            </sun:button>
            "&nbsp;
            <sun:button id="dirSelectBtn" 
		 text="$resource{i18n.button.BrowseFolder}" 
		 disabled="#{true}"
                 primary="#{false}" 
		 onClick="javascript: var win = window.open('../applications/fileChooser.jsf?folderOnly=true&dirPathId=#{dirPathId}&appNameId=#{appNameId}&ctxRootId=#{contextRootId}&typeId=#{typeId}&extensionId=#{extensionId}', 'fileChooserPopup', 'height=580, width=650, top='+((screen.height - (screen.height/1.618))-(500/2))+',left='+((screen.width-650)/2)+', scrollbars, resizable');win.focus(); return false" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{dirSelectBtnId});
                />
            </sun:button>
            <sun:textField  id="extension" visible="$boolean{false}" text="#{extension}">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{extensionId});
                />
            </sun:textField>
        </sun:property>  
        
    </sun:propertySheetSection>
</sun:propertySheet>    
