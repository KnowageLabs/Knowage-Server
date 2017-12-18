package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface HierarchyLevelAssocsReferenceLevel
  extends RefAssociation
{
  public abstract boolean exists(CwmLevel paramCwmLevel, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract CwmLevel getCurrentLevel(CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract Collection getHierarchyLevelAssociation(CwmLevel paramCwmLevel);
  
  public abstract boolean add(CwmLevel paramCwmLevel, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract boolean remove(CwmLevel paramCwmLevel, CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
}
