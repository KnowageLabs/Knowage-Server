package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmPackageClass
  extends RefClass
{
  public abstract CwmPackage createCwmPackage();
  
  public abstract CwmPackage createCwmPackage(String paramString, VisibilityKind paramVisibilityKind);
}
