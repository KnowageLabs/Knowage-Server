package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmClassClass
  extends RefClass
{
  public abstract CwmClass createCwmClass();
  
  public abstract CwmClass createCwmClass(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
