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
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/com/sun/enterprise/management/config/ConfigMgrTestBase.java,v 1.8 2006/11/11 00:53:59 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2006/11/11 00:53:59 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Collections;
import java.io.Serializable;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.MapUtil;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.config.AMXConfig;

import com.sun.appserv.management.client.ProxyFactory;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;
import com.sun.enterprise.management.CreateRemoveListener;

/**
 */
public abstract class ConfigMgrTestBase extends AMXTestBase
{
		protected
	ConfigMgrTestBase()
	{
		super();
	}
	
	
		protected String
	getProgenyTestName()
	{
		return( "test-" + ClassUtil.stripPackageName( this.getClass().getName() ) );
	}
	
	
	protected abstract AMXConfig	createProgeny(String name, Map<String,String> options );
	protected abstract Container	getProgenyContainer();
	protected abstract String		getProgenyJ2EEType();
	protected abstract void			removeProgeny( String name );
	
	protected static final Map<String,String>	ILLEGAL_OPTIONS	=
	    Collections.unmodifiableMap( MapUtil.newMap( new String[]
	{
		"IllegalParam1", "IllegalValue1",
		"IllegalParam2", "IllegalValue2",
	}));
	
		public final synchronized void
	testIllegalCreate() 
		throws Exception
	{
	    if ( ! checkNotOffline( "testIllegalCreate" ) )
	    {
	        return;
	    }
	    
		final String	name	= getProgenyTestName() + "-Illegal";
		
		try
		{
	    	final AMXConfig	proxy = createProgeny( name, ILLEGAL_OPTIONS );
	    	fail( "Expecting failure from createProgenyIllegal for progeny type: " + getProgenyJ2EEType() );
	    }
	    catch( final Exception e )
	    {
	    	final Throwable rootCause	= ExceptionUtil.getRootCause( e );
	    	if ( ! ( rootCause instanceof IllegalArgumentException ) )
	    	{
	    		warning( "expecting IllegalArgumentException, got: " + rootCause.getClass().getName() +
	    		", msg = " + rootCause.getMessage() );
	    		rootCause.printStackTrace();
	    	}
	    }
        catch( final Throwable t )
        {
	    	final Throwable rootCause	= ExceptionUtil.getRootCause( t );
            warning( "expecting IllegalArgumentException, got: " +  ExceptionUtil.toString(rootCause) );
            assert false;
        }
	}
	
		public final synchronized void
	testCreateRemove() 
		throws Exception
	{
	    if ( ! checkNotOffline( "testCreateRemove" ) )
	    {
	        return;
	    }
	    
		final long	start	= now();
		
		String	name	= getProgenyTestName();
		
		final String	progenyJ2EEType	= getProgenyJ2EEType();
		
		AMXConfig	proxy	= getProgeny( name );
		if ( proxy != null )
		{
			final ObjectName	objectName	= Util.getExtra( proxy ).getObjectName();
			remove( name );
	   		waitUnregistered( objectName );
	   		assert( ! getConnection().isRegistered( objectName ) );
			assert( getProgeny( name ) == null );
			proxy	= null;
		}
		
		final Container	container	= getProgenyContainer();
		final CreateRemoveListener		listener	=
			new CreateRemoveListener( container, progenyJ2EEType, name );
		
		// create it
		try
		{
	        proxy = createProgeny( name, null );
	    }
	    catch( Exception e )
	    {
	        trace( getStackTrace( ExceptionUtil.getRootCause(e) ) );
	        failure( "Can't create item of j2eeType=" + progenyJ2EEType +
	            ",name=" + name );
	    }
	    assert( proxy.getName().equals( name ) );
		final ObjectName	objectName	= Util.getObjectName( proxy );
	    assert( getConnection().isRegistered( objectName ) );
	    assert( container.getContainee( progenyJ2EEType, name)  != null  );
	    assert( container.getContainee( progenyJ2EEType, name)  == proxy  );
	    final AMXConfig	progeny	= getProgeny( name );
	    assert( progeny == proxy );

		// remove it
		final ProxyFactory	factory	= Util.getExtra( proxy ).getProxyFactory();
		assert( name.equals( progeny.getName() ) );
	    remove( name );
	    waitUnregistered( objectName );
	    assert( ! getConnection().isRegistered( objectName ) );
	    waitProxyGone( factory, objectName );
		assert( getProgeny( name ) == null );
		
		listener.waitNotifs();
		
		printElapsed( "testCreateRemove: created/remove/listen for: " + progenyJ2EEType, start );
	}


		public AMXConfig
	getProgeny( final String name )
	{
		final Container	container	= getProgenyContainer();
		final String			progenyType	= getProgenyJ2EEType();
		
		//trace( "getProgeny: " + progenyType + "=" + name );
		final Object	progeny	= container.getContainee( progenyType, name );
		
		if ( progeny != null && ! (progeny instanceof AMXConfig) )
		{
			assert( progeny instanceof AMX );
			failure(
				"getProgeny: " + progenyType + "=" + name + " not an AMXConfig, interface = " +
				Util.getExtra( Util.asAMX(progeny )).getInterfaceName() );
		}
		
		return( (AMXConfig)progeny );
	}


    
		protected void
	remove( final String name) 
	{
		removeProgeny( name );
	}

	
		void
	removeEx(String name) 
	{
		final AMX	proxy	= getProgeny( name );
		if ( proxy != null )
		{
			assert( proxy.getName().equals( name )  );
			final ObjectName	objectName	= Util.getObjectName( proxy );
			
		    try
		    {
		    	remove(name);
		    
			    final MBeanServerConnection	conn	= getConnection();
			    while ( conn.isRegistered( objectName ) )
			    {
			    	trace( "waiting for mbean to be unregistered: " + objectName );
			    }
		    }
		    catch(Exception e)
		    {
		    	trace( "error removing MBean: " +
		    		objectName + " = " + ExceptionUtil.getRootCause( e ).getMessage() );
		    }
	    }
	    else
	    {
	    	// trace( "ConfigMgrTestBase.removeEx: " + name + " does not exist." );
	    }
	}
}


