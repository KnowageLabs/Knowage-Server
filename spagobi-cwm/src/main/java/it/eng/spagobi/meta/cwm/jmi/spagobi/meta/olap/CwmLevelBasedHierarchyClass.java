package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmLevelBasedHierarchyClass
  extends RefClass
{
  public abstract CwmLevelBasedHierarchy createCwmLevelBasedHierarchy();
  
  public abstract CwmLevelBasedHierarchy createCwmLevelBasedHierarchy(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
