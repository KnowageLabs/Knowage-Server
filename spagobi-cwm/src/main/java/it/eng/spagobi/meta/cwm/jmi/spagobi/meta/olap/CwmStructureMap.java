package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation.CwmTransformationMap;

public abstract interface CwmStructureMap
  extends CwmTransformationMap
{
  public abstract CwmDimensionDeployment getDimensionDeployment();
  
  public abstract void setDimensionDeployment(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmDimensionDeployment getDimensionDeploymentLv();
  
  public abstract void setDimensionDeploymentLv(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmDimensionDeployment getDimensionDeploymentIp();
  
  public abstract void setDimensionDeploymentIp(CwmDimensionDeployment paramCwmDimensionDeployment);
}
