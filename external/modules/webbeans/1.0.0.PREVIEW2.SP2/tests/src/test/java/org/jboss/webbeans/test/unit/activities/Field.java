package org.jboss.webbeans.test.unit.activities;

import javax.enterprise.inject.Instance;

class Field
{
   
   @Tame Instance<Cow> instance;
   
   public Cow get()
   {
      return instance.get();
   }
   
}
