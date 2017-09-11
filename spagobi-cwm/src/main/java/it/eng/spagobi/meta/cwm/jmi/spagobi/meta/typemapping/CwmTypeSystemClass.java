package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTypeSystemClass
  extends RefClass
{
  public abstract CwmTypeSystem createCwmTypeSystem();
  
  public abstract CwmTypeSystem createCwmTypeSystem(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
