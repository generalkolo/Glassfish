/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.mock;

import java.io.IOException;
import java.net.URL;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.collections.EnumerationIterable;

public class MockResourceLoader implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      try
      {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
   public URL getResource(String name)
   {
      return Thread.currentThread().getContextClassLoader().getResource(name);
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         return new EnumerationIterable<URL>(Thread.currentThread().getContextClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
}
