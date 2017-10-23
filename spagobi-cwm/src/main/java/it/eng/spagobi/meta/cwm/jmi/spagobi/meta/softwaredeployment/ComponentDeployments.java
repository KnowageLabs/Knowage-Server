package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ComponentDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmComponent paramCwmComponent, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract CwmComponent getComponent(CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract Collection getDeployment(CwmComponent paramCwmComponent);
  
  public abstract boolean add(CwmComponent paramCwmComponent, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract boolean remove(CwmComponent paramCwmComponent, CwmDeployedComponent paramCwmDeployedComponent);
}
