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
 

package com.sun.appserv.management.ext.logging;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Date;

import java.util.logging.Level;

import java.io.Serializable;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;

/**
	Provides access to log messages already present in the log file.
	
	@since AS 9.0
 */
public interface LogQuery
{
    /**
        The lowest supported log level for which queries may be performed.
     */
    public static final String  LOWEST_SUPPORTED_QUERY_LEVEL  =
        Level.WARNING.toString();
    
	/**
		Query a server log file for records beginning at index <code>startIndex</code>.
		<p>
		The <code>name</code> parameter may be {@link LogFileAccess#MOST_RECENT_NAME}
		to query the current server log file, or may be any specific server log
		file as returned by {@link LogFileAccess#getLogFileNames}.
		<p>
		To query log records starting at the beginning of the file and moving forward,
		use startIndex={@link #FIRST_RECORD}.  To query records beginning at the end of the
		file and moving backwards, use startIndex={@link #LAST_RECORD} and
		specify <code>searchForward=false</code>.
		<p>
		If <code>searchForward</code> is true,
		then log records beginning with
		<code>startRecord</code> (inclusive) and later
		are considered by the query.<br>
		If <code>searchForward</code> is false,
		then log records beginning at
		<code>startRecord - 1</code> and earlier are considered by the query.
		<p>
		Because a log file  could be deleted
		<p>
		<b>QUESTIONS TO RESOLVE<b>
		<ul>
		<li>What are the legal keys and values of 'nameValueMap'</li>
		</ul>
		<p>

		@param name	a specific log file name or {@link LogFileAccess#MOST_RECENT_NAME}
		@param startIndex     the location within the LogFile to begin.
		@param searchForward  true to move forward, false to move backward from <code>startIndex</code>
		@param maxRecords  the maximum number of results to be returned, {@link #ALL_RECORDS} for all
		@param fromTime        the lower bound time, may be null (inclusive)
		@param toTime          the upper bound time, may be null (exclusive)
		@param logLevel        the minimum log level to return, see {@link Level}
		@param modules		   one or more modules as defined in {@link LogModuleNames} or
		                        any valid Logger name
		@param nameValuePairs    name-value pairs to match.  Names need not be unique.
		@return LogQueryResult  when using AMX client proxy.
		                        Actual type returned from the MBean is List&lt;Serializable[]>
		                        The first Serializable[] is a String[] which contains the field names.
		                        Subsequent Serializable[] each represent a log record with each element representing
		                        a field within that log record.
		@see LogRecordFields
		@see LogModuleNames
     */
        public LogQueryResult
    queryServerLog( 
    	String  name,
    	long     startIndex,
    	boolean searchForward,
        int     maxRecords,
        Long    fromTime,
        Long    toTime,
        String   logLevel,
        Set<String>      modules,
        List<Attribute> 	 nameValuePairs);
    
    /**
        Value for the <code>maximumNumberOfResults</code> parameter to
        {@link #queryServerLog} which returns all results.
     */
    public static final int ALL_RECORDS = -1;
     
    /**
        Value for the <code>startIndex</code> parameter to
        {@link #queryServerLog}.
     */
    public static final int   FIRST_RECORD   = 0;
    
    /**
        Value for the <code>startIndex</code> parameter to
        {@link #queryServerLog}.
     */
    public static final int   LAST_RECORD    = -1;
    
    /**
     */
    public String[] getDiagnosticCauses( String messageID );
    
    /**
     */
    public String[] getDiagnosticChecks( String messageID );
    
    
    /**
     */
    public String getDiagnosticURI( String messageID );
	
}






