/*
 * Copyright 2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */
/*
 * $Id: Data.java,v 1.2 2008/07/24 15:02:25 mullan Exp $
 */
package javax.xml.crypto;

import javax.xml.crypto.dsig.Transform;

/**
 * An abstract representation of the result of dereferencing a 
 * {@link URIReference} or the input/output of subsequent {@link Transform}s.
 * The primary purpose of this interface is to group and provide type safety
 * for all <code>Data</code> subtypes.
 *
 * @author Sean Mullan
 * @author JSR 105 Expert Group
 */
public interface Data { }
