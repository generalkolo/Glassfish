/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners;

import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.Bus;

/**
 * A listener for the Bus entity.
 * 
 * It implements the following annotations:
 * - None
 * 
 * It overrides the following annotations:
 * - PrePersist from ListenerSuperclass
 * - PostPersist from FueledVehicleListener
 * 
 * It inherits the following annotations:
 * - PostLoad from Vehicle.
 */
public class BusListener3 extends ListenerSuperclass {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;

	public void prePersist(Object bus) {
        PRE_PERSIST_COUNT++;
        ((Bus) bus).addPrePersistCalledListener(this.getClass());
	}
    
	public void postPersist(Object bus) {
        POST_PERSIST_COUNT++;
        ((Bus) bus).addPostPersistCalledListener(this.getClass());
	}
}
