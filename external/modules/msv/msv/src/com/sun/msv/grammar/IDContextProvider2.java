/*
 * @(#)$Id: IDContextProvider2.java 1565 2003-06-09 20:31:53Z kk122374 $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.grammar;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

import com.sun.msv.verifier.regexp.StringToken;

/**
 * ValidationContextProvider that supports limited ID/IDREF implementation.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface IDContextProvider2 extends ValidationContext {
    
    /**
     * this method is called when a type with ID semantics is matched.
     * 
     * It is the callee's responsibility that stores
     * ID and checks doubly defined ID, if it is necessary.
     */
    void onID( Datatype datatype, StringToken literal );
}
