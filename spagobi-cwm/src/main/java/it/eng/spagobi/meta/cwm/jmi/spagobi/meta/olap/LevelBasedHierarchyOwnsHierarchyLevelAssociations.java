package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface LevelBasedHierarchyOwnsHierarchyLevelAssociations
  extends RefAssociation
{
  public abstract boolean exists(CwmLevelBasedHierarchy paramCwmLevelBasedHierarchy, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract CwmLevelBasedHierarchy getLevelBasedHierarchy(CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract List getHierarchyLevelAssociation(CwmLevelBasedHierarchy paramCwmLevelBasedHierarchy);
  
  public abstract boolean add(CwmLevelBasedHierarchy paramCwmLevelBasedHierarchy, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract boolean remove(CwmLevelBasedHierarchy paramCwmLevelBasedHierarchy, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
}
