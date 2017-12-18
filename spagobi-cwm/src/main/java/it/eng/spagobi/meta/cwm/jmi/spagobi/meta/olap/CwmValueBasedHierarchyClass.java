package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmValueBasedHierarchyClass
  extends RefClass
{
  public abstract CwmValueBasedHierarchy createCwmValueBasedHierarchy();
  
  public abstract CwmValueBasedHierarchy createCwmValueBasedHierarchy(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
