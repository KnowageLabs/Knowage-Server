package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionDeploymentOwnsStructureMaps
  extends RefAssociation
{
  public abstract boolean exists(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract Collection getStructureMap(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract CwmDimensionDeployment getDimensionDeployment(CwmStructureMap paramCwmStructureMap);
  
  public abstract boolean add(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean remove(CwmStructureMap paramCwmStructureMap, CwmDimensionDeployment paramCwmDimensionDeployment);
}
