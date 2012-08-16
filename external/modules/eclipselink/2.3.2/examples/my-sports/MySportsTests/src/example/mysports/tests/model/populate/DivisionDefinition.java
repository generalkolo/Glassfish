/*******************************************************************************
 * Copyright (c) 2010-2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *  dclarke - EclipseLink 2.3 - MySports Demo Bug 344608
 ******************************************************************************/
package example.mysports.tests.model.populate;

class DivisionDefinition {
    String name;
    String[] teamNames;
    int numPlayers;
    
    public DivisionDefinition(String name, String[] teamNames, int numPlayers) {
        this.name = name;
        this.teamNames = teamNames;
        this.numPlayers = numPlayers;
    }

    public String getName() {
        return name;
    }

    public String[] getTeamNames() {
        return teamNames;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getTotalPlayers() {
        return getTeamNames().length * getNumPlayers();
    }

}