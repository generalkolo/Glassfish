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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/test/CLISupportTester.java,v 1.3 2005/12/25 03:45:52 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:52 $
 */
 
package com.sun.cli.jmx.test;

// java imports
//
import java.lang.reflect.Array;

// RI imports
//
import javax.management.*;
import javax.management.remote.JMXServiceURL;

import com.sun.jmx.trace.Trace;

import com.sun.cli.util.stringifier.*;
import com.sun.cli.util.ClassUtil;

import com.sun.cli.jmx.support.CLISupportStrings;
import com.sun.cli.jmx.support.StandardAliases;
import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.jmx.support.InspectRequest;
import com.sun.cli.jmx.support.InspectResult;
import com.sun.cli.jmx.support.InvokeResult;
import com.sun.cli.jmx.support.ResultsForGetSet;
import com.sun.cli.jmx.support.InspectResultStringifier;
import com.sun.cli.jmx.support.ResultsForGetSetStringifier;
import com.sun.cli.jmx.support.InvokeResultStringifier;


public final class CLISupportTester
{
	final MBeanServerConnection	mServer;
	final CLISupportMBeanProxy	mProxy;
	final TestLog				mLog;
	
	final static private String		ALIAS_BASE	= "test-alias-";
	private final static String		CLI_TEST_ALIAS_NAME	= ALIAS_BASE + "generic-test";

		public
	CLISupportTester( MBeanServerConnection conn, CLISupportMBeanProxy proxy )
		throws Exception
	{
		this( conn, proxy, new StdOutTestLog() );
	}
	
		public
	CLISupportTester( MBeanServerConnection conn, CLISupportMBeanProxy proxy, TestLog logger)
		throws Exception
	{
		mServer	= conn;
		mLog	= logger;
		
		mProxy	= proxy;
	}

		private void
	p( Object arg )
	{
		mLog.println( arg );
	}
	
		private void
	p( Object arg, boolean newline)
	{
		if ( newline )
		{
			p( arg );
		}
		else
		{
			mLog.print( arg );
		}
	}
	
		private void
	begin( String msg )
	{
		p( msg + "...", false);
	}
	
		private static String
	quote( Object str )
	{
		return( "\"" + str.toString() + "\"" );
	}
	

		private void
	TestMBeanList( CLISupportMBeanProxy test ) throws Exception
	{
		final String []	testStrings	= new String [] { StandardAliases.ALL_ALIAS };
		
		final ObjectName []	results	= test.mbeanFind( testStrings );
		final int			numObjects	= Array.getLength( results );
		
		final ArrayStringifier	testStringsStringifier	= new ArrayStringifier( "," );
		
		p( "\nmbean-list results: " + numObjects +
			" mbeans for expr " + testStringsStringifier.stringify( testStrings ) );
		
		final String	str	= ArrayStringifier.stringify( results, "\n", ObjectStringifier.DEFAULT);
		p ( str );
		
		p( "TestMBeanList...DONE: listed " + numObjects + " for " + testStrings[ 0 ] );
	}
	
		private void
	TestMBeanInspect( CLISupportMBeanProxy test, String [] patterns ) throws Exception
	{
		begin( "TestMBeanInspect" );
		
		final InspectRequest	request	= new InspectRequest();
		
		final InspectResult []	results	= test.mbeanInspect( request, patterns );
		final int			numResults	= Array.getLength( results );
		
		final String	summary	= "" + numResults +
				" mbeans for expr " + ArrayStringifier.stringify( patterns, ",");
				
		final String	str	= ArrayStringifier.stringify( results, "\n\n", new InspectResultStringifier());
		
		p( "DONE: inspected " + summary);
	}
	
		private void
	TestMBeanGet( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		begin( "TestMBeanGet" );
		
		final ResultsForGetSet []	results	= test.mbeanGet( "*", targets );
		final int					numResults	= Array.getLength( results );
		
		final String	summary	= "" + numResults +
				" mbeans for expr " + ArrayStringifier.stringify( targets, ",");
				
		final ResultsForGetSetStringifier resultStringifier =
			new ResultsForGetSetStringifier(  );
			
		final String str	= ArrayStringifier.stringify( results, "\n\n", resultStringifier);
		
		p ( str );
		
		p( "DONE: inspected " + summary);
	}
	
	
		private String
	InvokeResultsToString( InvokeResult []	results)
	{
		return( ArrayStringifier.stringify( results, "\n", new InvokeResultStringifier() ) );
	}
	
	
		private void
	testInvoke( String operationName, String args, String [] targets )
		throws Exception
	{
		InvokeResult []	results = null;
		
		results = mProxy.mbeanInvoke( operationName, args, targets );
		
		p( InvokeResultsToString( results ) + "\n" );
	}
	
		private void
	TestNamedInvoke( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		begin( "TestNamedInvoke" );
		
		testInvoke( "testNamed", "p1=hello", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there,p3=!!!", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there,p3=!!!,p4=foobar", targets );
		
		p( "DONE ");
	}


		private void
	TestMBeanInvoke( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		TestNamedInvoke( test, targets );
	}

	
	
		private void
	deleteTestAliases() throws Exception
	{
		final String []	aliases	= mProxy.listAliases( false );
		
		for( int i = 0; i < aliases.length; ++i )
		{
			final String	name	= aliases[ i ];
			
			if ( name.startsWith( ALIAS_BASE ) )
			{
				p( "deleteTestAliases: deleting: " + name );
				mProxy.deleteAlias( name );
			}
		}
	}
	
		private void
	TestAliases(  ) throws Exception
	{
		begin( "TestAliases" );
		
		deleteTestAliases();
		
		int	failureCount	= 0;
		
		// create an alias for each MBean
		final ObjectName []	names	= mProxy.mbeanFind( new String [] { StandardAliases.ALL_ALIAS } );
		final int			numNames	= Array.getLength( names );
		
		// create  test alias for each existing MBean
		for( int i = 0; i < numNames; ++i )
		{
			final String	aliasName	= ALIAS_BASE + (i+1);
			mProxy.createAlias( aliasName, names[ i ].toString() );
		}
		
		// now verify that each of them resolves correctly
		for( int i = 0; i < numNames; ++i )
		{
			final String	aliasName	= ALIAS_BASE + (i+1);
			
			final String	aliasValue	= mProxy.resolveAlias( aliasName );
			if ( aliasValue == null || ! names[ i ].toString().equals( aliasValue ))
			{
				++failureCount;
				p( "FAILURE: alias " + aliasName + ": " +
					quote( aliasValue ) + " != " + quote( names[ i ].toString() ) );
			}
		}
		
		// create an alias consisting of all aliases
		final String	ALL_ALIASES_NAME	= ALIAS_BASE + "all";
		final String []	aliases	= mProxy.listAliases( false );
		final String	allAliases	= ArrayStringifier.stringify( aliases, " " );
		mProxy.createAlias( ALL_ALIASES_NAME, allAliases );
		
		// create a recursive alias
		String	allAliasesName	= ALL_ALIASES_NAME;
		for( int i = 0; i < 5; ++i )
		{
			mProxy.createAlias( allAliasesName + i, allAliasesName );
			allAliasesName	= allAliasesName + i;
		}
		
		// verify that the alias to all of them produces the same set of names as we started with
		final ObjectName []	resolvedNames	= mProxy.resolveTargets( new String [] { allAliasesName } );
		//p( "all aliases = " + ArrayStringifier.stringify( resolvedNames, "\n" ) );
		if ( Array.getLength( resolvedNames ) !=  numNames )
		{
			++failureCount;
		}
		
		deleteTestAliases();
		
		if ( failureCount == 0 )
		{
			p( "DONE" );
		}
		else
		{
			p( "FAILURES = " + failureCount );
		}
	}
	
	/*
		Convert the parameters to String suitable for consumption by the CLISupportMBean
	 */
		private String
	MakeArgList( final String [] args )
	{
		final int			numArgs	= Array.getLength( args );
		String				result	= null;
		
		if ( numArgs != 0 )
		{
			final StringBuffer	buf	= new StringBuffer();
		
			for( int i = 0; i < numArgs; ++i )
			{
				buf.append( args[ i ] );
				buf.append( "," );
			}
			// strip trailing ","
			buf.setLength( buf.length() - 1 );
			
			result	= new String( buf ) ;
		}
		
		return( result );
	}
	
	
		private String
	getCastType( String type )
		throws ClassNotFoundException
	{
		String	result	= type;
		
		if ( ClassUtil.classnameIsArray( result ) )
		{
			final Class	theClass	= ClassUtil.getClassFromName(result);
			
			final Class elementClass	= ClassUtil.getInnerArrayElementClass( theClass );
			
			result	= elementClass.getName();
		}
		
		return( result );
	}
	
	
	
		private InvokeResult.ResultType
	TestOperationGenerically(
		final CLISupportMBeanProxy	test,
		final boolean				namedArgs,
		final ObjectName			targetName,
		final MBeanOperationInfo	operationInfo )
		throws Exception
	{
		final MBeanParameterInfo []	paramInfos	= operationInfo.getSignature();
		final int					numParams	= Array.getLength( paramInfos );
		
		final String []	strings	= new String [ numParams ];
		final String	operationName	= operationInfo.getName();
		
		// create an object of the correct type for each parameter.
		// The actual value is not important.
		for( int i = 0; i < numParams; ++i )
		{
			final MBeanParameterInfo	paramInfo	= paramInfos[ i ];
			final String				paramType	= paramInfos[ i ].getType();
			final Class					theClass	= ClassUtil.getClassFromName( paramType );
			
			final Object paramObject	= ClassUtil.InstantiateDefault( theClass );
			final String paramString	= SmartStringifier.toString( paramObject );
			final String castString		= "(" + getCastType( paramType ) + ")";
			
			final String paramName		= namedArgs ? (paramInfo.getName() + '=') : "";
			
			strings[ i ]	= paramName + castString + paramString;
		}
		
		// convert the arguments to strings
		final String	argString	= MakeArgList( strings );
		
		final String []	args	= new String [] { targetName.toString() };
		
		final InvokeResult []	results	= (InvokeResult [])test.mbeanInvoke( operationName, argString, args );
		final InvokeResult	result	= results[ 0 ];
		
		if ( result.getResultType() == InvokeResult.SUCCESS )
		{
			// p( "SUCCESS: " + operationName + "(" + SmartStringifier.toString( paramInfos ) + ")");
		}
		else
		{
			final String paramInfosString	= SmartStringifier.toString( paramInfos );
			
			p( "FAILURE: " + operationName + "(" + paramInfosString + ")" +
				" with " + argString );
			result.mThrowable.printStackTrace();
		}
		
		return( result.getResultType() );
	}
	
	static private final Class []	GENERICALLY_TESTABLE_CLASSES	= 
	{
		boolean.class,
		char.class,
		byte.class, short.class, int.class, long.class,
		float.class, double.class,
		Boolean.class,
		Character.class,
		Byte.class, Short.class, Integer.class, Long.class,
		Float.class,
		Double.class,
		Number.class,
		String.class,
		Object.class,
		java.math.BigDecimal.class,
		java.math.BigInteger.class,
		java.net.URL.class,
		java.net.URI.class

	};
		private boolean
	IsGenericallyTestableClass( final Class theClass )
		throws ClassNotFoundException
	{
		boolean	isTestable	= false;
		
		Class	testClass	= theClass;
		if ( ClassUtil.classIsArray( theClass ) )
		{
			// we can test all arrays of supported types
			testClass	= ClassUtil.getInnerArrayElementClass( theClass );
		}
		
		final Class []	classes	= GENERICALLY_TESTABLE_CLASSES;
		final int	numClasses	= Array.getLength( classes );
		for( int i = 0; i < numClasses; ++i )
		{
			if ( testClass == classes[ i ] )
			{
				isTestable	= true;
				break;
			}
		}
		
		if ( ! isTestable  )
		{
			assert( testClass == java.util.Properties.class );
		}
		
		return( isTestable );
	}
	
		private boolean
	IsGenericallyTestable( final MBeanOperationInfo operationInfo )
		throws ClassNotFoundException
	{
		boolean	isTestable	= true;
		
		final MBeanParameterInfo []	paramInfos	= operationInfo.getSignature();
		final int					numParams	= Array.getLength( paramInfos );
		for( int i = 0; i < numParams; ++i )
		{
			final Class	theClass	= ClassUtil.getClassFromName( paramInfos[i].getType() );
			
			if ( ! IsGenericallyTestableClass( theClass ) )
			{
				isTestable	= false;
				break;
			}
		}
		
		return( isTestable );
	}
	
	
		private void
	TestGeneric( CLISupportMBeanProxy test, boolean namedTest, ObjectName objectName ) throws Exception
	{
		final MBeanInfo				info	= mServer.getMBeanInfo( objectName );
		final MBeanOperationInfo []	opInfo	= info.getOperations();
		
		begin( "TestGeneric" );
		
		int	successCount	= 0;
		int	failureCount	= 0;
		int	notTestedCount	= 0;
		for( int i = 0; i < Array.getLength( opInfo ); ++i )
		{
			try
			{
				if ( IsGenericallyTestable( opInfo[ i ] ) )
				{
					final InvokeResult.ResultType resultType	= TestOperationGenerically( test,namedTest, objectName, opInfo[ i ] );
					if ( resultType == InvokeResult.SUCCESS )
					{
						++successCount;
					}
					else
					{
						++failureCount;
					}
				}
				else
				{
					++notTestedCount;
				}
			}
			catch( Exception e )
			{
				p( "FAILURE: " + SmartStringifier.toString( opInfo[ i ] ) );
			}
		}
		
		p( "DONE " + (namedTest ? "NAMED":"ORDERED") +
			": SUCCESSES = " + successCount +
			", FAILURES = " + failureCount +
			", NOT TESTABLE = " + notTestedCount );
	}
	
		private void
	TestGeneric( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		final ObjectName []	allObjects	= test.mbeanFind( targets );
		
		assert( allObjects.length >= 1 );
		
		for( int i = 0; i < allObjects.length; ++i )
		{
			TestGeneric( test, false, allObjects[ i ] );
		}
		
		for( int i = 0; i < allObjects.length; ++i )
		{
			TestGeneric( test, true, allObjects[ i ] );
		}
	}
	
		private void
	VerifySetup( CLISupportMBeanProxy proxy ) throws Exception
	{
		// must be at least one MBean
		final ObjectName []	all	= proxy.resolveTargets( new String [] { "*" } );
		assert( all.length != 0 );
		
		// verify that the AliasMgr and CLI are available.
		final String []	aliases	= proxy.listAliases( false );
		assert( aliases.length != 0 );
		
		// verify that required aliases are in place
		assert( proxy.resolveAlias( StandardAliases.ALL_ALIAS ) != null );
		assert( proxy.resolveAlias( StandardAliases.CLI_ALIAS ) != null );
		assert( proxy.resolveAlias( StandardAliases.ALIAS_MGR_ALIAS ) != null );
		
	}
	
	
		public void
	Run() throws Exception
	{
		final CLISupportMBeanProxy	proxy	= mProxy;
		
		try
		{
			// ensure certain aliases are present, as we use them
			final String []	all			= new String [] { StandardAliases.ALL_ALIAS };
		
			VerifySetup( proxy );
			
			TestAliases(  );
			
			TestMBeanList( proxy );
			
			TestMBeanGet( proxy, all );
			
			TestMBeanInspect( proxy, all );
			
			proxy.deleteAlias( CLI_TEST_ALIAS_NAME );
			proxy.createAlias( CLI_TEST_ALIAS_NAME, CLISupportStrings.CLI_SUPPORT_TESTEE_TARGET );
			final String []	testMBean	= new String [] { CLI_TEST_ALIAS_NAME };
			
			TestMBeanInvoke( proxy, testMBean );
			
			TestGeneric( proxy, testMBean);
			
			
			p( "DONE" );
			
				
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}


};


