/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import java.beans.PropertyChangeListener;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.ClassTransformer;
import javax.sql.DataSource;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.persistence.jpa.ProviderContainerContractInfo;

/**
 * Implements the internal GlassFish interface which all persistence provider
 * containers must.
 *
 * @author tjquinn
 */
public class ProviderContainerContractInfoImpl implements ProviderContainerContractInfo {

    private final URLClassLoader classLoader;
    private final Instrumentation inst;
    private final String applicationLocation;
    private final ConnectorRuntime connectorRuntime;

    private final List<PropertyChangeListener> transformerAdditionListeners =
            new ArrayList<PropertyChangeListener>();

    private final Collection<EntityManagerFactory> emfs = new HashSet<EntityManagerFactory>();
    /**
     * Creates a new instance of the ACC's implementation of the contract.
     * The ACC uses its agent to register a VM transformer which can then
     * delegate to transformers registered with this class by the
     * persistence logic.
     *
     * @param classLoader ACC's class loader
     * @param inst VM's instrumentation object
     */
    public ProviderContainerContractInfoImpl(
            final URLClassLoader classLoader,
            final Instrumentation inst,
            final String applicationLocation,
            final ConnectorRuntime connectorRuntime) {
        this.classLoader = classLoader;
        this.inst = inst;
        this.applicationLocation = applicationLocation;
        this.connectorRuntime = connectorRuntime;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ClassLoader getTempClassloader() {
        return new URLClassLoader(classLoader.getURLs());
    }

    public void addTransformer(ClassTransformer transformer) {
        inst.addTransformer(new TransformerWrapper(transformer, classLoader));
    }

    public String getApplicationLocation() {
        return applicationLocation;
    }

    public DataSource lookupDataSource(String dataSourceName) throws NamingException {
        return (DataSource) connectorRuntime.lookupPMResource(dataSourceName, true);
    }

    public DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException {
        return (DataSource) connectorRuntime.lookupPMResource(dataSourceName, true);
    }

    // TODO: remove after persistence is refactored.
    public DeploymentContext getDeploymentContext() {
        return null;
    }

    public boolean isDeploy() {
        // Returns whether an application is being deployed into server. For an AppClient it is always false
        return false;
    }

    public void registerEMF(String unitName, String persistenceRootUri, RootDeploymentDescriptor containingBundle, EntityManagerFactory emf) {
        emfs.add(emf);
    }

    public Collection<EntityManagerFactory> emfs() {
        return emfs;
    }

    /**
     * Wraps a persistence transformer in a java.lang.instrumentation.ClassFileTransformer
     * suitable for addition as a transformer to the JVM-provided instrumentation
     * class.
     */
    public static class TransformerWrapper implements ClassFileTransformer {

        private final ClassTransformer persistenceTransformer;
        private final ClassLoader classLoader;

        TransformerWrapper(final ClassTransformer persistenceTransformer,
                final ClassLoader classLoader) {
            this.persistenceTransformer = persistenceTransformer;
            this.classLoader = classLoader;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            /*
             * Do not even bother running the transformer unless the loader
             * loading the class is the ACC's class loader.
             */
            return (loader.equals(classLoader) ?
                persistenceTransformer.transform(loader, className,
                    classBeingRedefined, protectionDomain, classfileBuffer)
                : null);
        }
    }

}
