package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation.CwmTransformationMap;

public abstract interface CwmContentMap
  extends CwmTransformationMap
{
  public abstract CwmCubeDeployment getCubeDeployment();
  
  public abstract void setCubeDeployment(CwmCubeDeployment paramCwmCubeDeployment);
}
