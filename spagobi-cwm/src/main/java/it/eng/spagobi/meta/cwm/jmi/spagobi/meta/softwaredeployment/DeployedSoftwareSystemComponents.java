package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DeployedSoftwareSystemComponents
  extends RefAssociation
{
  public abstract boolean exists(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract Collection getDeployedSoftwareSystem(CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract Collection getDeployedComponent(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem);
  
  public abstract boolean add(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmDeployedComponent paramCwmDeployedComponent);
  
  public abstract boolean remove(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmDeployedComponent paramCwmDeployedComponent);
}
