package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMemberValueClass
  extends RefClass
{
  public abstract CwmMemberValue createCwmMemberValue();
  
  public abstract CwmMemberValue createCwmMemberValue(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
