package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface HierarchyLevelAssociationOwnsDimensionDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmDimensionDeployment paramCwmDimensionDeployment, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract List getDimensionDeployment(CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract CwmHierarchyLevelAssociation getHierarchyLevelAssociation(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean add(CwmDimensionDeployment paramCwmDimensionDeployment, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract boolean remove(CwmDimensionDeployment paramCwmDimensionDeployment, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
}
