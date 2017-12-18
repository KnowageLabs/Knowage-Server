package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMemberSetClass
  extends RefClass
{
  public abstract CwmMemberSet createCwmMemberSet();
  
  public abstract CwmMemberSet createCwmMemberSet(String paramString, VisibilityKind paramVisibilityKind);
}
