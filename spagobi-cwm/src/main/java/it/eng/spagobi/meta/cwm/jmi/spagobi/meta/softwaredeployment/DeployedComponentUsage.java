package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DeployedComponentUsage
  extends RefAssociation
{
  public abstract boolean exists(CwmDeployedComponent paramCwmDeployedComponent1, CwmDeployedComponent paramCwmDeployedComponent2);
  
  public abstract Collection getUsedComponents(CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract Collection getUsingComponents(CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract boolean add(CwmDeployedComponent paramCwmDeployedComponent1, CwmDeployedComponent paramCwmDeployedComponent2);
  
  public abstract boolean remove(CwmDeployedComponent paramCwmDeployedComponent1, CwmDeployedComponent paramCwmDeployedComponent2);
}
