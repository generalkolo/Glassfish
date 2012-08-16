/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.proxyindirection;


/*
 * Large Project interface.
 *
 * Define behavior for Large Project objects.
 *
 * @author        Rick Barkhouse
 * @since        08/24/2000 11:11:05
 */
public interface LargeProject extends Project {
    public double getBudget();

    public String getInvestor();

    public void setBudget(double value);

    public void setInvestor(String value);
}