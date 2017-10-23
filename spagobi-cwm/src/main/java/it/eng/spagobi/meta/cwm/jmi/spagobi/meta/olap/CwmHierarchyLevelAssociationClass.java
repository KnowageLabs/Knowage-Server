package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmHierarchyLevelAssociationClass
  extends RefClass
{
  public abstract CwmHierarchyLevelAssociation createCwmHierarchyLevelAssociation();
  
  public abstract CwmHierarchyLevelAssociation createCwmHierarchyLevelAssociation(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
