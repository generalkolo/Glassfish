/*
 * @(#)$Id: XSDBatchTest.java 1567 2003-06-09 20:50:21Z kk122374 $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.verifier;

import junit.framework.TestSuite;

/**
 * tests the entire RELAX test suite by using BatchVerifyTester.
 * 
 * for use by automated test by ant.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class XSDBatchTest {
    public static TestSuite suite() throws Exception {
        return new BatchVerifyTester().createFromProperty(
            "xsd","XSDBatchTestDir");
    }
}
