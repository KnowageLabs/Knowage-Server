package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionDeploymentHasImmediateParent
  extends RefAssociation
{
  public abstract boolean exists(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmStructureMap getImmediateParent(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmDimensionDeployment getDimensionDeploymentIp(CwmStructureMap paramCwmStructureMap);
  
  public abstract boolean add(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean remove(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
}
