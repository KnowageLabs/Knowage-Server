package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ValueBasedHierarchyOwnsDimensionDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmDimensionDeployment paramCwmDimensionDeployment, CwmValueBasedHierarchy paramCwmValueBasedHierarchy);
  
  public abstract List getDimensionDeployment(CwmValueBasedHierarchy paramCwmValueBasedHierarchy);
  
  public abstract CwmValueBasedHierarchy getValueBasedHierarchy(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean add(CwmDimensionDeployment paramCwmDimensionDeployment, CwmValueBasedHierarchy paramCwmValueBasedHierarchy);
  
  public abstract boolean remove(CwmDimensionDeployment paramCwmDimensionDeployment, CwmValueBasedHierarchy paramCwmValueBasedHierarchy);
}
