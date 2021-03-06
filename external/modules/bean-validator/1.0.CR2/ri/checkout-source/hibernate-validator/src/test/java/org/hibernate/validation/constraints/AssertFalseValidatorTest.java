// $Id: AssertFalseValidatorTest.java 16249 2009-04-02 12:10:40Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.constraints;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 */
public class AssertFalseValidatorTest {

	private static AssertFalseValidator constraint;

	@BeforeClass
	public static void init() {
		constraint = new AssertFalseValidator();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( false, null ) );
		assertTrue( constraint.isValid( Boolean.FALSE, null ) );
		assertFalse( constraint.isValid( true, null ) );
		assertFalse( constraint.isValid( Boolean.TRUE, null ) );
	}
}
