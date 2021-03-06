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

package com.sun.enterprise.webservice;

import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.message.saaj.SAAJMessage;

import com.sun.enterprise.webservice.SOAPMessageContext;
import java.util.jar.Pack200;

import javax.xml.soap.SOAPMessage;

/**
 * Implementation of SOAPMessageContext
 */
public class SOAPMessageContextImpl implements SOAPMessageContext {

    private Packet packet = null;
    private SOAPMessage message = null;

    public SOAPMessageContextImpl(Packet pkt) {
        this.packet = pkt;
    }
    
    public void setPacket(Packet packet) {
        this.packet = packet;
        this.message = null;
    }

    public SOAPMessage getMessage() {
        
        if (message != null) {
            return message;
        }
        
        SOAPMessage soapMsg = null;
        try {
            //before converting to SOAPMessage, make a copy.  We don't want to consume
            //the original message
            Message mutable = packet.getMessage().copy();
            soapMsg = mutable.readAsSOAPMessage();
                       
        } catch (Exception e) {
            e.printStackTrace();
        }
       
 	//store the message so we don't have to convert again
	message = soapMsg; 

        return soapMsg;
    }

    public void setMessage(SOAPMessage newMsg) {
        message = newMsg;
        
        //keep the com.sun.xml.ws.api.message.Message in the packet consistent with the
        //SOAPMessage we are storing here.
        packet.setMessage(Messages.create(newMsg));
    }
    
    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public Set<String> getRoles() {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public Scope getScope(String name) {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public void setScope(String name, Scope scope) {
        // this is a dummy impl; we do not use it at all
        return;
    }

    public boolean isAlreadySoap() {
        // In jaxws-rearch, only SOAP messages come here
        // So always return true
        return true;
    }
    
    /* java.util.Map methods below here */

    public void clear() {
        // We just clear whatever we set; we do not clear jaxws's properties'
        packet.invocationProperties.clear();
    }

    public boolean containsKey(Object obj) {
        // First check our property bag
        if(packet.supports(obj)) {
            return packet.containsKey(obj);
        }
        return packet.invocationProperties.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        return packet.invocationProperties.containsValue(obj);
    }

    public Set<Entry<String, Object>> entrySet() {
        return packet.invocationProperties.entrySet();
    }

    public Object get(Object obj) {
        if(packet.supports(obj)) {
            return packet.get(obj);
        }
        return packet.invocationProperties.get(obj);
    }

    public boolean isEmpty() {
        return packet.invocationProperties.isEmpty();
    }

    public Set<String> keySet() {
        return packet.invocationProperties.keySet();
    }

    public Object put(String str, Object obj) {
        return packet.invocationProperties.put(str, obj);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        packet.invocationProperties.putAll(map);
    }

    public Object remove(Object obj) {
        return packet.invocationProperties.remove(obj);
    }

    public int size() {
        return packet.invocationProperties.size();
    }

    public Collection<Object> values() {
        return packet.invocationProperties.values();
    }
}
