package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmModelClass
  extends RefClass
{
  public abstract CwmModel createCwmModel();
  
  public abstract CwmModel createCwmModel(String paramString, VisibilityKind paramVisibilityKind);
}
