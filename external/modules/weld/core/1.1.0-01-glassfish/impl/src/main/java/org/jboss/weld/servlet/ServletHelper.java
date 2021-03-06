/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.servlet;

import static org.jboss.weld.logging.messages.ServletMessage.BEAN_MANAGER_NOT_FOUND;
import static org.jboss.weld.logging.messages.ServletMessage.CONTEXT_NULL;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author pmuir
 *
 */
public class ServletHelper
{
   
   public static BeanManagerImpl getModuleBeanManager(ServletContext ctx)
   {
      if (ctx == null)
      {
         throw new IllegalArgumentException(CONTEXT_NULL);
      }
      BeanManagerImpl beanManagerImpl = (BeanManagerImpl) ctx.getAttribute(BeanManager.class.getName());
      if (beanManagerImpl == null)
      {
         throw new IllegalArgumentException(BEAN_MANAGER_NOT_FOUND, ctx);
      }
      else
      {
         return beanManagerImpl;
      }
   }

}
