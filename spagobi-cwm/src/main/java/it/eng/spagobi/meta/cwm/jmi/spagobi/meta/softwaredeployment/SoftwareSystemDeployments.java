package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SoftwareSystemDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract Collection getDeployment(CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract CwmSoftwareSystem getSoftwareSystem(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem);
  
  public abstract boolean add(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract boolean remove(CwmDeployedSoftwareSystem paramCwmDeployedSoftwareSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
}
