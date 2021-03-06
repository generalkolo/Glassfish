/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.util.lookup.implspi;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the active reference queue.
 * @since 8.1
 */
public final class ActiveQueue {

    private ActiveQueue() {}

    private static final Logger LOGGER = Logger.getLogger(ActiveQueue.class.getName());
    private static Impl activeReferenceQueue;

    /**
     * Gets the active reference queue.
     * @return the singleton queue
     */
    public static synchronized ReferenceQueue<Object> queue() {
        if (activeReferenceQueue == null) {
            activeReferenceQueue = new Impl();
        }

        activeReferenceQueue.ping();

        return activeReferenceQueue;
    }

    private static final class Impl extends ReferenceQueue<Object> implements Runnable {
        /** number of known outstanding references */
        private int count;

        Impl() {
        }

        @Override
        public Reference<Object> poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reference<Object> remove(long timeout) throws IllegalArgumentException, InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public Reference<Object> remove() throws InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Reference<?> ref = super.remove(0);
                    LOGGER.finer("dequeued reference");
                    if (!(ref instanceof Runnable)) {
                        LOGGER.log(Level.WARNING, "A reference not implementing runnable has been added to the Utilities.activeReferenceQueue(): {0}", ref.getClass());
                        continue;
                    }
                    // do the cleanup
                    try {
                        ((Runnable) ref).run();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Should not happen.
                        // If it happens, it is a bug in client code, notify!
                        LOGGER.log(Level.WARNING, "Cannot process " + ref, t);
                    } finally {
                        // to allow GC
                        ref = null;
                    }
                } catch (InterruptedException ex) {
                    // Can happen during VM shutdown, it seems. Ignore.
                    continue;
                }
                synchronized (this) {
                    assert count > 0;
                    count--;
                    if (count == 0) {
                        // We have processed all we have to process (for now at least).
                        // Could be restarted later if ping() called again.
                        // This could also happen in case someone called queue() once and tried
                        // to use it for several references; in that case run() might never be called on
                        // the later ones to be collected. Can't really protect against that situation.
                        // See issue #86625 for details.
                        LOGGER.fine("stopping thread");
                        break;
                    }
                }
            }
        }

        synchronized void ping() {
            if (count == 0) {
                Thread t = new Thread(this, "Active Reference Queue Daemon");
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
                LOGGER.fine("starting thread");
            } else {
                LOGGER.finer("enqueuing reference");
            }
            count++;
        }

    }

}
