package org.jboss.webbeans.test.unit.environments.servlet;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.inject.Named;

@Stateful
@Tame
@Named("Pongo")
class Hound implements HoundLocal
{ 
   @Remove
   public void bye() {
   }

   public void ping()
   {
      
   }
   
}
