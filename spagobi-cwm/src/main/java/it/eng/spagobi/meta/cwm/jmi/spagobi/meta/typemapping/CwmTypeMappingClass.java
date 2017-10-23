package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTypeMappingClass
  extends RefClass
{
  public abstract CwmTypeMapping createCwmTypeMapping();
  
  public abstract CwmTypeMapping createCwmTypeMapping(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2);
}
