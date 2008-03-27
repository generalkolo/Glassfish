/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.Security;
import javax.net.ssl.SSLSocketFactory;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;

import com.sun.cli.jmxcmd.security.sasl.UserPasswordCallbackHandler;

import java.security.cert.CertificateException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;
import java.security.Security;
import java.security.Provider;
import javax.security.sasl.Sasl;

import com.sun.cli.jmxcmd.security.sasl.DigestMD5ClientCallbackHandler;
import com.sun.cli.jmxcmd.security.sasl.SaslClientSetup;

import com.sun.cli.jcmd.util.misc.ClassUtil;
import com.sun.cli.jcmd.util.misc.GSetUtil;
import com.sun.cli.jmxcmd.security.TLSSetup;

/**
	Supports connectivity to JSR 160 jmxmp servers.
	<p>
	Supported protocols are {@link #JMXMP_PROTOCOL
}
 */
public final class JMXMPDefaultConnectorProvider
	extends JMXConnectorProviderBase
{
		public
	JMXMPDefaultConnectorProvider()
	{
	}
	
	static class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements the standard JSR 160 connector.";
		private static final String	USAGE	=
			"connect [--host <host>] --port port --protocol jmxmp " +
			"[--user <user> --password-file <path> ] "  +
			"[--trust-store <path>] " + "[connection-name]";
		
			public String
		getDescription()
		{
			return( DESCRIPTION );
		}
			public String
		getUsage()
		{
			return( USAGE );
		}
	}
	
		public static JMXConnectorProviderInfo
	getInfo()
	{
		return( new Info() );
	}
	
	
	
	
		public JMXConnector
	connect( java.util.Map m )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException,
		KeyManagementException, UnrecoverableKeyException
	{
		final String	host		= (String)m.get( HOST );
		final String	port		= (String)m.get( PORT );
		
		requireParam( host, "host" );
		requireParam( port, "port" );
		
		final String	user			= (String)m.get( USER );
		final String	password		= (String)m.get( PASSWORD );
		final String	trustStoreFile	= (String)m.get( TRUSTSTORE_FILE );
		final String	trustStorePassword	= (String)m.get( TRUSTSTORE_PASSWORD );
		final String	sasl			= (String)m.get( SASL );
		
        final boolean	useTLS	= trustStoreFile != null;
		final HashMap	env	= new HashMap();
		
		final SaslClientSetup	setup	= new SaslClientSetup( env, useTLS );
		if ( useTLS )
		{
			TLSSetup.setupTLSForJMXMP( env, new File( trustStoreFile ), trustStorePassword, null);
		}
           
        setup.setupProfiles( sasl );
		setup.setupClientCallback( user, password );
		
		final String		urlString	= "service:jmx:jmxmp://" + host + ":" + port;
		
		return( connect( urlString, env ) );
	}
	
	public final static String	JMXMP_PROTOCOL	= "jmxmp";
	public static final Set<String>	SUPPORTED_PROTOCOLS	=
		GSetUtil.newUnmodifiableStringSet( JMXMP_PROTOCOL );
	
		protected Set
	getSupportedProtocols()
	{
		return( SUPPORTED_PROTOCOLS );
	}
}




