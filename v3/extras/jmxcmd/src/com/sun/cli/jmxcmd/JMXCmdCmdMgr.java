/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/JMXCmdCmdMgr.java,v 1.19 2005/05/20 00:45:16 llc Exp $
 * $Revision: 1.19 $
 * $Date: 2005/05/20 00:45:16 $
 */
 
package com.sun.cli.jmxcmd;

import java.io.File;
import java.util.Map;
import java.util.Properties;


import com.sun.cli.jcmd.framework.CmdFactoryIniter;
import com.sun.cli.jcmd.JCmdKeys;
import com.sun.cli.jcmd.framework.CmdFactory;
import com.sun.cli.jcmd.framework.CmdMgrImpl;
import com.sun.cli.jcmd.framework.CmdSource;
import com.sun.cli.jcmd.framework.CmdEnvKeys;

// to get the Cmd classes we need to register
import com.sun.cli.jmxcmd.cmd.*;

import com.sun.cli.jcmd.util.stringifier.StringifierRegistryImpl;
import com.sun.cli.jmxcmd.support.StringifierRegistryIniter;

import com.sun.cli.jcmd.util.misc.ExceptionUtil;
import com.sun.cli.jcmd.util.misc.ClassUtil;

/**
 */
public final class JMXCmdCmdMgr extends CmdMgrImpl
{
        private void
    checkRequirements()
    {
        try
        {
            Class.forName( "javax.management.j2ee.statistics.CountStatistic" );
        }
        catch( Exception e )
        {
            mCmdOutput.println( "WARNING: javax.management.j2ee.statistics package missing; " +
            "add javax77.jar to classpath" );
        }
        
        
        try
        {
            Class.forName( "javax.management.remote.generic.GenericConnector" );
        }
        catch( Exception e )
        {
            mCmdOutput.println(
                "WARNING: javax.management.remote.generic package missing; " +
                "add jmxremote_optional.jar to classpath" );
        }
    }
    
	/**
	 */
		public
	JMXCmdCmdMgr( final Map metaOptions )
		throws Exception
	{
		super( metaOptions );
		
		checkRequirements();
		
		try
		{
			mCmdOutput.printDebug( "aliases file = " + JMXCmd.getAliasesFile() );
			
			new StringifierRegistryIniter( StringifierRegistryImpl.DEFAULT );
				
			mGreeting	= GREETING;
			
			if ( mCmdOutput.getDebug() )
			{
				final Properties	props	=
					(Properties)mMetaOptions.get( JCmdKeys.PROPERTIES );
				
				final String value	= props.getProperty( JMXCmdEnvKeys.DEBUG_CONNECTION );
				
				final boolean	debugConnection	= Boolean.TRUE.toString().equals( value );
				
				mCmdEnv.put( JMXCmdEnvKeys.DEBUG_CONNECTION, "" + debugConnection, false);
			}
			
			mCmdFactory.setUnknownCmdClassGetter(
				new MyCmdClassGetter( mCmdFactory.getUnknownCmdClassGetter()) );
			
			getEnv().put( CmdEnvKeys.UNKNOWN_CMD_HELPER, new UnknownCmdHelperImpl(), false);
		}
		catch (Throwable t )
		{
			System.err.println( ExceptionUtil.getStackTrace( t ) );
			throw (Exception)t;
		}

	}
	
	private class MyCmdClassGetter implements CmdFactory.UnknownCmdClassGetter
	{
		final CmdFactory.UnknownCmdClassGetter	mGetter;
		
			public
		MyCmdClassGetter( CmdFactory.UnknownCmdClassGetter next )
		{
			mGetter	= next;
		}
		
			public Class
		getCmdClass( String name )
		{
			Class	theClass	= mGetter.getCmdClass( name );
			if ( theClass == null )
			{
				theClass	= InvokeCmd.class;
			}
			
			return( theClass );
		}
	}

	
	
	private class JMXCmdCmdSource	implements CmdSource
	{
		JMXCmdCmdSource()	{}
		public Class[]		getClasses( )	{ return( BUILT_IN_COMMANDS );	}
		
		private final Class [] BUILT_IN_COMMANDS =
		{
			GetCmd.class,
			SetCmd.class,
			FindCmd.class,
			InspectCmd.class,
			MBeanCmd.class,
			ListenCmd.class,
			MonitorCmd.class,
			InvokeCmd.class,
			TargetAliasesCmd.class,
			TargetCmd.class,
			ConnectCmd.class,
			CountCmd.class,
			DomainsCmd.class,
			ProvidersCmd.class,
			JMXCmdVersionCmd.class,
			MBeanServerCmd.class,
			ValidateMBeansCmd.class,
			GenerateMBeansCmd.class,
			ProxyCmd.class,
			SecurityCmd.class
		};
	}
	
		
		
		protected void
	initCmds()
		throws Exception
	{
		super.initCmds();
		
		final CmdFactoryIniter	initer = new CmdFactoryIniter( mCmdFactory, new JMXCmdCmdSource() );
	}

	
	private final static String GREETING =
	"Type 'help' for help, 'quit' to quit.\n";
	
}

