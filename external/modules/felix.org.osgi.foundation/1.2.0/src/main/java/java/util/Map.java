/*
 * $Header: /cvshome/build/ee.foundation/src/java/util/Map.java,v 1.6 2006/03/14 01:20:25 hargrave Exp $
 *
 * (C) Copyright 2001 Sun Microsystems, Inc.
 * Copyright (c) OSGi Alliance (2001, 2005). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;
public abstract interface Map {
	public abstract void clear();
	public abstract boolean containsKey(java.lang.Object var0);
	public abstract boolean containsValue(java.lang.Object var0);
	public abstract java.util.Set entrySet();
	public abstract boolean equals(java.lang.Object var0);
	public abstract java.lang.Object get(java.lang.Object var0);
	public abstract int hashCode();
	public abstract boolean isEmpty();
	public abstract java.util.Set keySet();
	public abstract java.lang.Object put(java.lang.Object var0, java.lang.Object var1);
	public abstract void putAll(java.util.Map var0);
	public abstract java.lang.Object remove(java.lang.Object var0);
	public abstract int size();
	public abstract java.util.Collection values();
	public static abstract interface Entry {
		public abstract boolean equals(java.lang.Object var0);
		public abstract java.lang.Object getKey();
		public abstract java.lang.Object getValue();
		public abstract int hashCode();
		public abstract java.lang.Object setValue(java.lang.Object var0);
	}
}

