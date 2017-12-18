package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface HierarchyMemberSelectionGroupReferencesHierarchy
  extends RefAssociation
{
  public abstract boolean exists(CwmHierarchy paramCwmHierarchy, CwmHierarchyMemberSelectionGroup paramCwmHierarchyMemberSelectionGroup);
  
  public abstract Collection getHierarchy(CwmHierarchyMemberSelectionGroup paramCwmHierarchyMemberSelectionGroup);
  
  public abstract Collection getHierarchyMemberSelectionGroup(CwmHierarchy paramCwmHierarchy);
  
  public abstract boolean add(CwmHierarchy paramCwmHierarchy, CwmHierarchyMemberSelectionGroup paramCwmHierarchyMemberSelectionGroup);
  
  public abstract boolean remove(CwmHierarchy paramCwmHierarchy, CwmHierarchyMemberSelectionGroup paramCwmHierarchyMemberSelectionGroup);
}
