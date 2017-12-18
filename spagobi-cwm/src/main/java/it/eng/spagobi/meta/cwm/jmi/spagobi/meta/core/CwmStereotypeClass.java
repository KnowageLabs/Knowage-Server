package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmStereotypeClass
  extends RefClass
{
  public abstract CwmStereotype createCwmStereotype();
  
  public abstract CwmStereotype createCwmStereotype(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
