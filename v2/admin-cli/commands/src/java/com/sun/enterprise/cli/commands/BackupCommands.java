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

package com.sun.enterprise.cli.commands;

import java.io.*;
import java.util.*;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.util.diagnostics.ObjectAnalyzer;
//import com.sun.enterprise.util.diagnostics.SystemProps;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.backup.BackupException;
import com.sun.enterprise.config.backup.BackupManager;
import com.sun.enterprise.config.backup.RestoreManager;
import com.sun.enterprise.config.backup.BackupRequest;
import com.sun.enterprise.config.backup.BackupWarningException;
import com.sun.enterprise.config.backup.ListManager;

import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.pluggable.ClientPluggableFeatureFactory;
/**
 *  This is a local command for backing-up domains.
 *  @version  $Revision: 1.3 $
 * The Options:
	<ul>
	<li>domaindir
	</ul>
 * The Operand:
	<ul>
	<li>domain
	</ul>
 */

public class BackupCommands extends BaseLifeCycleCommand
{
	public String toString()
	{
		return super.toString() + "\n" + ObjectAnalyzer.toString(this);
	}
	
	/**
	 *  An abstract method that validates the options
	 *  on the specification in the xml properties file
	 *  @return true if successfull
	 */
	public boolean validateOptions() throws CommandValidationException
	{
		super.validateOptions();
		setOptions();
		checkOptions();
		prepareRequest();
		
		// if anything went wrong, an Exception would have been thrown 
		// and we'd never get to this return statement...
		return true;
	}
	/**
	 *  An abstract method that executes the command
	 *  @throws CommandException
	 */
	public void runCommand() throws CommandException, CommandValidationException
	{
		validateOptions();
		
		try
		{
			if(command == CmdType.BACKUP)
			{
				BackupManager mgr = new BackupManager(request);
				CLILogger.getInstance().printMessage(mgr.backup());
			}
			else if(command == CmdType.RESTORE)
			{
				RestoreManager mgr = new RestoreManager(request);
				CLILogger.getInstance().printMessage(mgr.restore());
			}
			else if(command == CmdType.LIST)
			{
				ListManager mgr = new ListManager(request);
				CLILogger.getInstance().printMessage(mgr.list());
			}
			else
			{
				// IMPOSSIBLE!!!
				throw new CommandException("Internal Error");
			}
		}
		catch(BackupWarningException bwe)
		{
			CLILogger.getInstance().printMessage(bwe.getMessage());
		}
		catch(BackupException be)
		{
			throw new CommandException(be);
		}
	}
	

	/**
	 *  A method that sets the options and operand that the user supplied.
	 */
	private void setOptions() throws CommandValidationException
	{
		setCommand();
		setDomainsDir();
		setDomainName();
		setBackupFilename();
		setDescription();
		setVerbosity();
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void setCommand()  throws CommandValidationException
	{
		String cmd = getName();
		command = CmdType.valueOf(cmd);
		
		if(command == null)
		{
			// This shouldn't happen unless somebody erred editing CLIDescriptor.xml
			throw new CommandValidationException(
				getLocalizedString("NoUsageText", new String[] {cmd}) );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void setDomainName() throws CommandValidationException
	{
		try
		{
			domainName = getDomainName();
		}
		catch(CommandException ce)
		{
			throw new CommandValidationException(ce);
		}
		//domainName = (String)operands.firstElement();
	}

	///////////////////////////////////////////////////////////////////////////

	private void setDomainsDir() throws CommandValidationException
	{
		domainsDir = getOption(DOMAINSDIR);

		if(domainsDir == null || domainsDir.length() <= 0)
			domainsDir = System.getProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setBackupFilename()
	{
		// this option is only used for restore operations
		backupFilename = getOption(FILENAME);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setVerbosity() throws CommandValidationException
	{
		if(getBooleanOption("terse"))
			terse = true;

		if(getBooleanOption("verbose"))
			verbose = true;
		
		// it is an error for both to be true (duh!)
		
		if(verbose && terse)
			throw new CommandValidationException(getLocalizedString("NoVerboseAndTerseAtTheSameTime"));
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setDescription()
	{
		description = getOption(DESCRIPTION);
	}
	
	/**
	 * A method that checks the options and operand that the user supplied.
	 * These tests are slightly different for different CLI commands
	 */
	private void checkOptions() throws CommandValidationException
	{
		// disallow backup & restore if server is running.  list-backups is OK anytime...
		if(command == CmdType.BACKUP || command == CmdType.RESTORE)
		{
			if(!isNotRunning())
			{
				throw new CommandValidationException(getLocalizedString("DomainIsNotStopped",
					new String[] {command.name} ));
			}
		}
		// make sure we have a domainsDir
		if(domainsDir == null || domainsDir.length() <= 0)
		{
			throw new CommandValidationException(getLocalizedString("InvalidDomainPath",
				new String[] {domainsDir}) );
		}

		File domainsDirFile = new File(domainsDir);

		// make sure domainsDir exists and is a directory
		if(!domainsDirFile.isDirectory())
		{
			throw new CommandValidationException(getLocalizedString("InvalidDomainPath",
				new String[] {domainsDir}) );
		}

		File domainFile = new File(domainsDirFile, domainName);

		// BACKUP, LIST: make sure the domain dir exists and is 
		//              a directory and is writable
		// RESTORE: It must exist if backupFilename isn't set.
		boolean domainDirDoesNotHaveToExist = 
			(command == CmdType.RESTORE) && backupFilename != null;
							
		if(!domainDirDoesNotHaveToExist)
		{
			if(!domainFile.isDirectory() || !domainFile.canWrite())
			{
				throw new CommandValidationException(getLocalizedString("InvalidDirectory",
					new String[] {domainFile.getPath()}) );
			}
		}
		
		if(backupFilename != null)
		{
			File f = new File(backupFilename);
			
			if(!f.exists() || !f.canRead())
			{
				throw new CommandValidationException(getLocalizedString("FileDoesNotExist",
					new String[] { backupFilename } ));
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void prepareRequest() throws CommandValidationException
	{
		if(backupFilename == null)
			request = new BackupRequest(domainsDir, domainName, description);
		else
			request = new BackupRequest(domainsDir, domainName, description, backupFilename);
		
		request.setTerse(terse);
		request.setVerbose(verbose);
	}
	
	///////////////////////////////////////////////////////////////////////////

	private boolean isNotRunning() throws CommandValidationException
	{
		try
		{
			ClientPluggableFeatureFactory	cpff	= getFeatureFactory();
			DomainsManager					dm		= cpff.getDomainsManager();
			DomainConfig					dc		= getDomainConfig(domainName);
			InstancesManager				im		= dm.getInstancesManager(dc);
			final int						state	= im.getInstanceStatus();

			return state == Status.kInstanceNotRunningCode;
		}
		catch(Exception e)
		{
			throw new CommandValidationException(e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/* temp debug code TBD FIXME 
	private void dumpProps()
	{
		List list = SystemProps.get();
		
		for(Iterator it = list.iterator(); it.hasNext(); )
		{
			Map.Entry entry = (Map.Entry)it.next();
			
			if(((String)entry.getKey()).startsWith("com.sun."))
				System.out.println((String)entry.getKey() + "=" + (String)entry.getValue());
		}
	}
	*/	
	///////////////////////////////////////////////////////////////////////////

	private static final	String			DOMAINSDIR	= "domaindir";
	private static final	String			FILENAME	= "filename";
	private static final	String			DESCRIPTION	= "description";
	private					BackupRequest	request;
	private					String			domainName;
	private					String			domainsDir;
	private					String			backupFilename;
	private					String			description;
	private					CmdType			command;
	private					boolean			terse	= false;
	private					boolean			verbose	= false;
	
	private static class CmdType
	{
		private CmdType(String name)
		{
			this.name = name;
		}
		private static CmdType valueOf(String aName)
		{
			if(aName.equals(BACKUP.name))
				return BACKUP;

			if(aName.equals(RESTORE.name))
				return RESTORE;
			
			if(aName.equals(LIST.name))
				return LIST;

			return null;
		}
		
		private static	final	CmdType BACKUP	= new CmdType("backup-domain");
		private static	final	CmdType RESTORE	= new CmdType("restore-domain");
		private static	final	CmdType LIST	= new CmdType("list-backups");
		private			final	String	name;
	}
}
