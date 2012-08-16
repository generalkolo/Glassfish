/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sessions.coordination.corba.sun;


/**
* org/eclipse/persistence/internal/remotecommand/corba/sun/_SunCORBAConnectionStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from rcm.idl
* Tuesday, March 30, 2004 2:00:14 PM EST
*/
public class _SunCORBAConnectionStub extends org.omg.CORBA.portable.ObjectImpl implements org.eclipse.persistence.internal.sessions.coordination.corba.sun.SunCORBAConnection {
    public byte[] executeCommand(byte[] commandData) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("executeCommand", true);
            org.eclipse.persistence.internal.sessions.coordination.corba.sun.CommandDataHelper.write($out, commandData);
            $in = _invoke($out);
            byte[] $result = org.eclipse.persistence.internal.sessions.coordination.corba.sun.CommandDataHelper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return executeCommand(commandData);
        } finally {
            _releaseReply($in);
        }
    }
    // executeCommand

    // Type-specific CORBA::Object operations
    private static String[] __ids = { "IDL:org/eclipse/persistence/internal/remotecommand/corba/sun/SunCORBAConnection:1.0" };

    public String[] _ids() {
        return __ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException {
        String str = s.readUTF();
        String[] args = null;
        java.util.Properties props = null;
        org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init(args, props).string_to_object(str);
        org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
        _set_delegate(delegate);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        String[] args = null;
        java.util.Properties props = null;
        String str = org.omg.CORBA.ORB.init(args, props).object_to_string(this);
        s.writeUTF(str);
    }
}// class _SunCORBAConnectionStub