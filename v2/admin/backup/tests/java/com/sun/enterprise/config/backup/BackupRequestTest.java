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
 * BackupRequestTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 1:23 PM
 */

package com.sun.enterprise.config.backup;

import com.sun.enterprise.config.backup.*;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;
import junit.framework.*;

/**
 *
 * @author Byron Nevins
 */
public class BackupRequestTest extends TestCase
{
	
	public BackupRequestTest(java.lang.String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite(BackupRequestTest.class);
		return suite;
	}
	
	/** Test of verifyInfo method, of class com.sun.enterprise.config.backup.BackupRequest. */
	public void testBadCtor()
	{
		System.out.println("testVerifyInfo");
		try
		{
			BackupRequest req = new BackupRequest(null, null);
			BackupManager mgr = new BackupManager(req);
		}
		catch(NullPointerException e)
		{
			System.out.println(e);
			return;
		}
		catch(BackupException be)
		{
			System.out.println(be);
			return;
		}
		fail("Should have got an Exception!");
	}

	public void testBadDomainsDir()
	{
		System.out.println("testBadDomainsDir");
		try
		{
			BackupRequest req = new BackupRequest("w:/foo", "goo");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			return;
		}
		fail("Should have got an Exception!");
	}

	public void testGoodDomainsDir()
	{
		System.out.println("testGoodDomainsDir");
		File f = new File("c:/temp/goo");
		f.mkdirs();
		try
		{
			BackupRequest req = new BackupRequest("c:/temp", "goo");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			fail("Should not have got an Exception!");
		}
		finally
		{
			FileUtils.whack(f);
		}
	}
	public void testBadDomainName()
	{
		System.out.println("testBadDomainName");
		try
		{
			BackupRequest req = new BackupRequest("c:/temp", "");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			return;
		}
		fail("Should have got an Exception!");
	}
	public void testGoodDomainNameButNotExisting()
	{
		System.out.println("testGoodDomainName");
		try
		{
			File f = new File("c:/temp/goo");
			FileUtils.whack(f);
			BackupRequest req = new BackupRequest("c:/temp", "goo");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			return;
		}
		
		fail("Should have got an Exception!");
	}
	public void testGoodDomainName()
	{
		System.out.println("testGoodDomainName");
		File f = new File("c:/temp/goo");
		f.mkdirs();
		try
		{
			BackupRequest req = new BackupRequest("c:/temp", "goo");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			fail("Should not have got an Exception!");
		}
		finally
		{
			FileUtils.whack(f);
		}
	}

	public void testBadBackupFile()
	{
		System.out.println("testBadBackupFile");
		File f = new File("c:/temp/goo");
		try
		{
			f.mkdirs();
			BackupRequest req = new BackupRequest("c:/temp", "goo", "c:/temp/q.zip");
			BackupManager mgr = new BackupManager(req);
		}
		catch(BackupException be)
		{
			System.out.println(be);
			return;
		}
		finally
		{
			FileUtils.whack(f);
		}
		fail("Should have got an Exception!");
	}
	
	public static void main(java.lang.String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}
}
