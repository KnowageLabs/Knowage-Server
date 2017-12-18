package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ComponentsOnMachine
  extends RefAssociation
{
  public abstract boolean exists(CwmMachine paramCwmMachine, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract CwmMachine getMachine(CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract Collection getDeployedComponent(CwmMachine paramCwmMachine);
  
  public abstract boolean add(CwmMachine paramCwmMachine, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract boolean remove(CwmMachine paramCwmMachine, CwmDeployedComponent paramCwmDeployedComponent);
}
