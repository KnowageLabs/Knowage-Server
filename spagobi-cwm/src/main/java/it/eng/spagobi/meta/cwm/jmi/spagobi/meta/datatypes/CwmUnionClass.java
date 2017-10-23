package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmUnionClass
  extends RefClass
{
  public abstract CwmUnion createCwmUnion();
  
  public abstract CwmUnion createCwmUnion(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
