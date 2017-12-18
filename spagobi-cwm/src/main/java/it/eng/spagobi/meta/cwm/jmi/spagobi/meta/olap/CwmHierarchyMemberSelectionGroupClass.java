package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmHierarchyMemberSelectionGroupClass
  extends RefClass
{
  public abstract CwmHierarchyMemberSelectionGroup createCwmHierarchyMemberSelectionGroup();
  
  public abstract CwmHierarchyMemberSelectionGroup createCwmHierarchyMemberSelectionGroup(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
