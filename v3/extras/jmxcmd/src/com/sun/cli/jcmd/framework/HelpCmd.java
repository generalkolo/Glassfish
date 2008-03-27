/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/HelpCmd.java,v 1.11 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jcmd.framework;

import java.util.Arrays;
import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.Cmd;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdFactory;
import com.sun.cli.jcmd.framework.CmdEnvImpl;
import com.sun.cli.jcmd.framework.CmdBase;


public class HelpCmd extends CmdBase
{
		public
	HelpCmd( final CmdEnv env )
	{
		super( env );
	}
	
		protected CmdFactory
	getCmdFactory()
	{
		return( super.getCmdFactory() );
	}
	
		Cmd
	instantiateCmd( Class cmdClass )
		throws Exception
	{
		Cmd		cmd	= null;
		
		final CmdEnvImpl	env	= new CmdEnvImpl();
		env.put( CmdEnvKeys.TOKENS, new String[0], false);
		
		final String[]	names	= getCmdNames( cmdClass );
		if ( names != null )
		{
			cmd	= getCmdFactory().createCmd( names[ 0 ], cmdClass, env );
		}
		
		return( cmd );
	}
	
		CmdHelp
	getHelpForCmd( Class cmdClass )
		throws Exception
	{
		CmdHelp	help	= null;
		
		if ( cmdClass == this.getClass() )
		{
			help	= new HelpCmdItselfHelp();
		}
		else if ( cmdClass != null )
		{
			final Cmd	cmd	= instantiateCmd( cmdClass );
			if ( cmd != null )
			{
				help	= cmd.getHelp();
			}
		}
		
		return( help );
	}
	
	class HelpCmdItselfHelp extends CmdHelpImpl
	{
		public	HelpCmdItselfHelp()	{ super( getCmdInfos() ); }
		
		private final static String	HELP_NAME		= "help";
		private final static String	SYNOPSIS		= "display help";
		private final static String	HELP_TEXT		=
	"To see all commands, type 'help'.  To see help for a particular command, " +
	"type 'help cmd'.";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( HELP_TEXT ); }
		
			public String
		toString()
		{
			return( HELP_TEXT );
		}
	}
	
	final class HelpAllCommandsHelp extends CmdHelpImpl
	{
		public	HelpAllCommandsHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "display help";
		private final static String	HELP_TEXT		=
	"To see all commands, type 'help'.  To see help for a particular command, " +
	"type 'help cmd'.";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( HELP_TEXT ); }
		
			public String
		toString()
		{
			final CmdFactory	factory	= getCmdFactory();
			
			final Class[]		classes		= factory.getClasses();
			final CmdInfos[]	cmdInfos	= new CmdInfos[ classes.length ];
			final CmdHelp[]		cmdHelps	= new CmdHelp[ classes.length ];
			
			for( int i = 0; i < classes.length; ++i )
			{
				try
				{
					cmdInfos[ i ]	= getCmdInfos( classes[ i ] );
					cmdHelps[ i ]	= getHelpForCmd( classes[ i ] );
				}
				catch( Exception e )
				{
					cmdHelps[ i ]	= null;
					e.printStackTrace();
				}
			}
			
			final String[]	helps	= new String[ classes.length ];
			for( int classIdx = 0; classIdx < classes.length; ++classIdx )
			{
				helps[ classIdx ]	= cmdHelps[ classIdx ].getName() +
					": " + cmdHelps[ classIdx ].getSynopsis();
				
				final int	numSubCmds	= cmdInfos[ classIdx ].size();
				if ( numSubCmds <= 1 )
				{
				}
				else
				{
					for( int subCmdIdx = 0; subCmdIdx < numSubCmds; ++ subCmdIdx )
					{
						helps[ classIdx ]	+= "\n    " + cmdInfos[ classIdx ].get( subCmdIdx ).getName();
					}
				}
			}
			
			Arrays.sort( helps );
			return( ArrayStringifier.stringify( helps, "\n\n" ) );
		}
	}

		public CmdHelp
	getHelp()
	{
		return( new HelpAllCommandsHelp() );
	}
	

private final static String	HELP_NAME1		= "help";
private final static String	HELP_NAME2		= "-?";
private final static String	HELP_NAME3		= "--help";
	
		private String
	stripColon( String cmd )
	{
		String	cmdString	= cmd;
		
		if ( cmdString.endsWith( ":" ) )
		{
			// indicates generic JMX method
			cmdString	= cmdString.substring( 0, cmdString.length() -1);
		}
		return( cmdString );
	}
	
		protected String
	getHelpUnknown( String cmdString )
	{
		return( "no help available for command: " + cmdString );
	}
	
	
	static final OperandsInfo	OPERANDS_INFO =
		new OperandsInfoImpl("command-name", 0 );
	
	private static final CmdInfo	HELP_INFO1	= new CmdInfoImpl( HELP_NAME1, null, OPERANDS_INFO );
	private static final CmdInfo	HELP_INFO2	= new CmdInfoImpl( HELP_NAME2 );
	private static final CmdInfo	HELP_INFO3	= new CmdInfoImpl( HELP_NAME3  );
	
		public static CmdInfos
	getCmdInfos( )
	{
		// name can vary so new one up dynamically
		return( new CmdInfos( HELP_INFO1, HELP_INFO2, HELP_INFO3) );
	}
	
		Class
	searchForCmdClass( String cmdName )
		throws Exception
	{
		final CmdFactory	cmdFactory	= getCmdFactory();
		
		Class	result	= cmdFactory.getClass( cmdName );
		if ( result == null )
		{
			final Class[]	classes	= cmdFactory.getClasses();
			
			for( int i = 0; i < classes.length; ++i )
			{
				final Cmd		cmd		= instantiateCmd( classes[ i ] );
				final CmdHelp	help	= cmd.getHelp();
				
				// check if the overall name matches
				if ( help.getName().equals( cmdName ) )
				{
					result	= classes[ i ];
					break;
				}
			}
		}
		
		return( result );
	}
		
		protected String
	buildHelpMessage( UnknownCmdHelper helper )
		throws Exception
	{
		final String []	 	operands	= getOperands();
		final CmdFactory	cmdFactory	= getCmdFactory();
		String				msg	= null;
		
		for ( int i = 0; i < operands.length; ++i )
		{
			final String	cmd	= operands[ i ];
			
			Class	cmdClass	= cmdFactory.getClass( cmd );
			if ( cmdClass == null )
			{
				cmdClass	= searchForCmdClass( cmd );
			}
			
			CmdHelp	help	= getHelpForCmd( cmdClass );
			/*
			if ( help == null )
			{
				final Class cmdClass	= cmdFactory.getClass( cmd );
				
				if ( cmdClass != null )
				{
					final String [] aka	= getCmdNames( cmdClass );
					if ( aka != null && aka.length != 0)
					{
						help	= getHelpForCmd( cmdFactory.getClass( aka[ 0 ] ) );
					}
				}
			}
			*/
			
			
			if ( help != null )
			{
				msg	= help.toString();
				
				cmdClass	= cmdFactory.getClass( cmd );
				if ( cmdClass != null )
				{
					msg	= msg + "\n" + getAlsoKnownAs( cmdClass );
				}
			}
			else if ( helper != null )
			{
				msg	= helper.getHelpUnknown( cmd );
			}
		}
		return( msg );
	}
	
	public interface UnknownCmdHelper
	{
		public String	getHelpUnknown(String cmd );
	}
	
		public static final UnknownCmdHelper
	getUnknownCmdHelper()
	{
		return( UnknownCmdHelperImpl.getInstance() );
	}
	
	static final class UnknownCmdHelperImpl implements UnknownCmdHelper
	{
		public	UnknownCmdHelperImpl()	{}
			public String
		getHelpUnknown( String cmd )
		{
			return( "Unknown command: " + cmd );
		}
		
		private static final UnknownCmdHelperImpl	INSTANCE	= new UnknownCmdHelperImpl();
			public static UnknownCmdHelperImpl
		getInstance()
		{
			return( INSTANCE );
		}
	}
	
	/**
		Handle a help request. If help cannot be found, use helper to determine
		what to do.
	 */
		public void
	handleHelp( UnknownCmdHelper helper)
		throws Exception
	{
		if ( getArgHelper() == null )
		{
			preExecute();
		}
		
		final String [] operands	= getOperands();
		
		if ( operands.length == 0 )
		{
			printUsage();
		}
		else
		{
			println( buildHelpMessage( helper ) );
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		printDebug( "HelpCmd.executeInternal " );
		
		UnknownCmdHelper	helper	=
			(UnknownCmdHelper)envGet( CmdEnvKeys.UNKNOWN_CMD_HELPER );
		if ( helper == null )
		{
			helper	= UnknownCmdHelperImpl.getInstance();
		}
		
		handleHelp( helper );
	}
}
