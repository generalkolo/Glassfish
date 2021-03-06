/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns;

import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorColumn;

/**
 * Object to hold onto discriminator column metadata.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataDiscriminatorColumn  {    
    public static final int DEFAULT_LENGTH = 31;
    public static final String DEFAULT_NAME = "DTYPE";
    public static final String DEFAULT_COLUMN_DEFINITION = "";
    public static final String DEFAULT_DISCRIMINATOR_TYPE = DiscriminatorType.STRING.name();
    
    protected DiscriminatorColumn m_discriminatorColumn;
    
    /**
     * INTERNAL:
     */
    protected MetadataDiscriminatorColumn() {}
    
    /**
     * INTERNAL:
     */
    public MetadataDiscriminatorColumn(DiscriminatorColumn discriminatorColumn) {
        m_discriminatorColumn = discriminatorColumn;
    }

    /**
     * INTERNAL:
     */
    public String getColumnDefinition() {
        return (m_discriminatorColumn == null) ? DEFAULT_COLUMN_DEFINITION : m_discriminatorColumn.columnDefinition();
    }
    
    /**
     * INTERNAL:
     */
    public String getDiscriminatorType() {
        return (m_discriminatorColumn == null) ? DEFAULT_DISCRIMINATOR_TYPE : m_discriminatorColumn.discriminatorType().name();
    }
    
    /**
     * INTERNAL:
     */
    public int getLength() {
        return (m_discriminatorColumn == null) ? DEFAULT_LENGTH : m_discriminatorColumn.length();
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return (m_discriminatorColumn == null) ? null : m_discriminatorColumn.name();
    }
}
