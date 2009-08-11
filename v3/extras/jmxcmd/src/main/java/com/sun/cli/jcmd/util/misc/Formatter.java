/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/Formatter.java,v 1.3 2005/11/08 22:39:22 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:22 $
 */
 
package com.sun.cli.jcmd.util.misc;


import java.text.MessageFormat;

import com.sun.cli.jcmd.util.misc.ClassUtil;

import org.glassfish.admin.amx.util.stringifier.Stringifier;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistry;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;

/**
	Escapes/unescapes strings
 */
public final class Formatter implements StringSource
{
	final StringSource			mStringSource;
	final StringifierRegistry	mRegistry;
	
		public
	Formatter( StringSource source )
	{
		mStringSource	= source;
		mRegistry		= StringifierRegistryImpl.DEFAULT;
	}
	
		public Object
	prepare( Object o )
	{
		Object	result	= o;
		
		if ( mRegistry.lookup( o.getClass() ) != null ||
			ClassUtil.objectIsArray( o ) )
		{
			result	= SmartStringifier.toString( o );
		}
		
		return( result );
	}
	
	/**
		Prepare objects for formatting. Certain objects are not properly handled by MesageFormat
		(such as Sets and arrays). We'll intercept these types, but leave dates and numbers
		to MessageFormat.
	 */
		private Object[]
	prepare( Object[] objects )
	{
		final Object[]	results	= new Object[ objects.length ];
		
		for( int i = 0; i < objects.length; ++i )
		{
			results[ i ]	= prepare( objects[ i ] );
		}
		
		return( results );
	}
	
	/**
		Format the objects into a String using the pattern specified by 'key'.
		
		@param key		key used to look up the pattern
		@param objects	array of objects to insert into the pattern
	 */
		public String
	format( String key, Object[] objects )
	{
		return( MessageFormat.format( getString( key ), prepare( objects ) ) );
	}
	
		public String
	format( String key, Object o1 )
	{
		return( format( key, new Object[] { o1 } ) );
	}
	
		public String
	format( String key, Object o1, Object o2)
	{
		return( format( key, new Object[] { o1, o2 } ) );
	}
	
		public String
	format( String key, Object o1, Object o2, Object o3)
	{
		return( format( key, new Object[] { o1, o2, o3} ) );
	}
	
		public String
	format( String key, Object o1, Object o2, Object o3, Object o4)
	{
		return( format( key, new Object[] { o1, o2, o3, o4 } ) );
	}
	
	
		public String
	getString( String id )
	{
		return( mStringSource.getString( id ) );
	}
	
		public String
	getString( String id, String defaultValue)
	{
		return( mStringSource.getString( id, defaultValue ) );
	}
}


