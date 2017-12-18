package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMemberClass
  extends RefClass
{
  public abstract CwmMember createCwmMember();
  
  public abstract CwmMember createCwmMember(String paramString, VisibilityKind paramVisibilityKind);
}
