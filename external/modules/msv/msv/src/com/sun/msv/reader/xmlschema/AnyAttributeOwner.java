/*
 * @(#)$Id: AnyAttributeOwner.java 1566 2003-06-09 20:37:49Z kk122374 $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.reader.xmlschema;

import com.sun.msv.grammar.xmlschema.AttributeWildcard;

/**
 * A state that can have &lt;anyAttribute &gt; element must implement this interface.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface AnyAttributeOwner
{
    /**
     * Sets the attribtue wildcard.
     */
    void setAttributeWildcard( AttributeWildcard local );
}
