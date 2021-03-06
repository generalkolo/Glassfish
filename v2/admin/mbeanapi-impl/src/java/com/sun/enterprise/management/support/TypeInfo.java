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
package com.sun.enterprise.management.support;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import javax.management.ObjectName;

import com.sun.appserv.management.base.AllTypesMapper;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.ObjectUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;

/**
	Information mapping a j2eeType to other necessary information.
 */
public final class TypeInfo
{
	private final TypeData	mTypeData;
	private final Set<String>	mChildJ2EETypes;
	private final Set<String>	mNonChildJ2EETypes;
	
	private final Class		mInterface;	
	private final Class		mImplClass;
        
		public
	TypeInfo( final TypeData	typeData )
		throws ClassNotFoundException
	{
		mTypeData			= typeData;
		mInterface			= deriveInterface( typeData.getJ2EEType() );
		mImplClass			= deriveImplClass( mInterface );
		
		mChildJ2EETypes		= new HashSet<String>();
		mNonChildJ2EETypes	= new HashSet<String>();
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final String PR	= "\n  ";
		
		buf.append( getJ2EEType() + ": " );
		buf.append( PR + "parent type = " + SmartStringifier.toString( getLegalParentJ2EETypes() ) );
		buf.append( PR + "child types = " + SmartStringifier.toString( mChildJ2EETypes ) );
		buf.append( PR + "mbean = " + mInterface.getName() );
		buf.append( PR + "impl = " + mImplClass.getName() );
		buf.append( "\n" );
		
		return( buf.toString() );
	}
	
	
		private static Class
	deriveInterface( final String j2eeType )
		throws ClassNotFoundException
	{
		return( AllTypesMapper.getInstance().getInterfaceForType( j2eeType ) );
	}
	
		private static String
	getBaseName( String mbeanClassName )
	{
		final String classname	= ClassUtil.stripPackageName( mbeanClassName );
		final String baseName	= classname;
		
		return( baseName );
	}
	
	
	private static final String[]	IMPL_PACKAGES	= new String[]
	{
        "com.sun.enterprise.management.config",
		"com.sun.enterprise.management.monitor",
		"com.sun.enterprise.management.j2ee",
		"com.sun.enterprise.management.support",
		"com.sun.enterprise.management.ext.lb",
		"com.sun.enterprise.management.ext.wsmgmt",
		"com.sun.enterprise.management.ext.logging",
		"com.sun.enterprise.management.deploy",
		"com.sun.enterprise.management.ext",
		"com.sun.enterprise.management",
	};
	
	private final static String IMPL    = "Impl";
	
		private static Class
	locateImplClass(
		final String	packageName,
		final String	baseName )
	{
	    if ( ! packageName.startsWith( "com.sun.enterprise.management" ) )
	    {
	        throw new RuntimeException( "Illegal implementation package for AMX" );
	    }

		final String implClassname	= packageName + "." + baseName + IMPL;
		
		Class	implClass	= null;
		try
		{
			implClass	= ClassUtil.getClassFromName( implClassname );
		}
		catch( ClassNotFoundException e )
		{
			// ignore, we'll return null
		}
		
		return( implClass );
	}
	
    /**
        Individual cases.
     */
        private static Map<String,String>
    getInterfaceToImplMap()
    {
        final Map<String,String>    m   = new HashMap<String,String>();
        
        m.put( "com.sun.appserv.management.DomainRoot", "com.sun.enterprise.management.DomainRootImpl" );
        m.put( "com.sun.appserv.management.ext.logging.Logging", "com.sun.enterprise.management.ext.logging.LoggingImpl" );
        m.put( "com.sun.appserv.management.deploy.DeploymentMgr", "com.sun.enterprise.management.deploy.DeploymentMgrImpl" );
        m.put( "com.sun.appserv.management.ext.lb.LoadBalancer", "com.sun.enterprise.management.ext.lb.LoadBalancerImpl" );
        m.put( "com.sun.appserv.management.ext.wsmgmt.WebServiceMgr", "com.sun.enterprise.management.ext.wsmgmt.WebServiceMgrImpl" );
        m.put( "com.sun.appserv.management.ext.update.UpdateStatus", "com.sun.enterprise.management.ext.update.UpdateStatusImpl" );
        
        return Collections.unmodifiableMap( m );
    }
    private static final Map<String,String> INTERFACE_TO_IMPL   = getInterfaceToImplMap();
        
		private static Class
	deriveImplClass( final Class mbeanInterface )
		throws ClassNotFoundException
	{
        final String fullyQualifiedName = mbeanInterface.getName();
		final String shortName	= getBaseName( fullyQualifiedName );
        
		Class	implClass	= null;
        
        // optimize for speed, calling locateImplClass() is very expensive
        // (4-5ms per class on a really fast machine)
        if ( fullyQualifiedName.startsWith( "com.sun.appserv.management.config" ) )
        {
            implClass   = locateImplClass( "com.sun.enterprise.management.config", shortName );
        }
        else if ( fullyQualifiedName.startsWith( "com.sun.appserv.management.monitor" ) )
        {
            implClass   = locateImplClass( "com.sun.enterprise.management.monitor", shortName );
        }
        else if ( fullyQualifiedName.startsWith( "com.sun.appserv.management.j2ee" ) )
        {
            implClass   = locateImplClass( "com.sun.enterprise.management.j2ee", shortName );
        }
        else if ( fullyQualifiedName.startsWith( "com.sun.appserv.management.base" ) )
        {
            implClass   = locateImplClass( "com.sun.enterprise.management.support", shortName );
        }
        else if ( INTERFACE_TO_IMPL.containsKey( fullyQualifiedName ) )
        {
            implClass   = ClassUtil.getClassFromName( INTERFACE_TO_IMPL.get( fullyQualifiedName ) );
        }
        else
        { 
            // as of 30 Nov 2006, there were only 5 exceptions, handled by INTERFACE_TO_IMPL map
           //System.out.println( "NEEDED: " + mbeanInterface );
        }
		
        if ( implClass == null )
        {
            for( int i = 0; i < IMPL_PACKAGES.length; ++i )
            {
                implClass	= locateImplClass( IMPL_PACKAGES[ i ], shortName );
                
                if ( implClass != null )
                {
                    break;
                }
            }
        }
		
		if ( implClass == null )
		{
			throw new ClassNotFoundException(
			    "Expected to find implementation class " + shortName + IMPL );
		}
		
		return( implClass );
	}
	
	
		public void
	addChildJ2EEType( final String j2eeType )
	{
		assert( ! j2eeType.equals( getJ2EEType() ) );
		assert( ! mNonChildJ2EETypes.contains( j2eeType ) );
		
		mChildJ2EETypes.add( j2eeType );
	}
	
		public void
	addContaineeJ2EEType( final String j2eeType )
	{
		assert( ! j2eeType.equals( getJ2EEType() ) );
		assert( ! mChildJ2EETypes.contains( j2eeType ) );
		
		mNonChildJ2EETypes.add( j2eeType );
	}
	
		public String
	getJ2EEType()
	{
		return( mTypeData.getJ2EEType() );
	}
	
		public Set<String>
	getLegalParentJ2EETypes()
	{
		return( mTypeData.getLegalParentJ2EETypes() );
	}
	
		public String
	getContainedByJ2EEType()
	{
		return( mTypeData.getContaineeByJ2EEType() );
	}
	
		public boolean
	isSubType()
	{
		return( mTypeData.isSubType() );
	}
	
		public Class
	getInterface()
	{
		return( mInterface );
	}
	
		public Class
	getImplClass()
	{
		return( mImplClass );
	}
	
		public String
	getParentJ2EEType()
	{
		final Set<String>	legalParentJ2EETypes	= getLegalParentJ2EETypes();
		
		if ( legalParentJ2EETypes == null )
		{
			throw new IllegalArgumentException( "no legal parent types for: " + getJ2EEType() );
		}
		
		if ( legalParentJ2EETypes.size() != 1 )
		{
			throw new IllegalArgumentException( "expecting single parent for " +
			getJ2EEType() + ", have: " + toString( legalParentJ2EETypes ) );
		}
		
		return( GSetUtil.getSingleton( legalParentJ2EETypes ) );
	}


		public Set<String>
	getChildJ2EETypes()
	{
		return( Collections.unmodifiableSet( mChildJ2EETypes ) );
	}
	
		public Set<String>
	getNonChildJ2EETypes()
	{
		return( Collections.unmodifiableSet( mNonChildJ2EETypes ) );
	}
	
		public Set<String>
	getContaineeJ2EETypes()
	{
		final Set<String>	all	=
		    GSetUtil.newSet( mChildJ2EETypes, mNonChildJ2EETypes );
		
		return( Collections.unmodifiableSet( all ) );
	}
 	
 	    public int
 	hashCode()
 	{
 	    return ObjectUtil.hashCode(
 	            mTypeData, mInterface, mChildJ2EETypes, mNonChildJ2EETypes );
 	}
	
		public boolean
	equals( final Object o )
	{
		if ( o == this )
		{
			return( true );
		}
		else if ( ! (o instanceof TypeInfo ) )
		{
			return( false );
		}
		
		final TypeInfo	rhs	= (TypeInfo)o;
		boolean	equals	= false;
		if ( mTypeData.equals( rhs.mTypeData ) &&
			mInterface == rhs.mInterface &&
			mImplClass	== rhs.mImplClass &&
			mChildJ2EETypes.equals( rhs.mChildJ2EETypes ) &&
			getLegalParentJ2EETypes().equals( rhs.getLegalParentJ2EETypes() )
				)
		{
			equals	= true;
		}
		
		return( equals );
	}
	
	
		private static String
	toString( final Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	
}








