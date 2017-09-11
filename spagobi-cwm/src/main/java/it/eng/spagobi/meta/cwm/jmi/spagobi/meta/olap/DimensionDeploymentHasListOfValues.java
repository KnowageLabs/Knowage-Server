package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionDeploymentHasListOfValues
  extends RefAssociation
{
  public abstract boolean exists(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmStructureMap getListOfValues(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmDimensionDeployment getDimensionDeploymentLv(CwmStructureMap paramCwmStructureMap);
  
  public abstract boolean add(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean remove(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
}
