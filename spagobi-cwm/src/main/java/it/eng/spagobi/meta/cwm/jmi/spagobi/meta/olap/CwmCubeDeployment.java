package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmCubeDeployment
  extends CwmClass
{
  public abstract CwmCubeRegion getCubeRegion();
  
  public abstract void setCubeRegion(CwmCubeRegion paramCwmCubeRegion);
  
  public abstract CwmDeploymentGroup getDeploymentGroup();
  
  public abstract void setDeploymentGroup(CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract Collection getContentMap();
}
