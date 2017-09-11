package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmDependencyClass
  extends RefClass
{
  public abstract CwmDependency createCwmDependency();
  
  public abstract CwmDependency createCwmDependency(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
