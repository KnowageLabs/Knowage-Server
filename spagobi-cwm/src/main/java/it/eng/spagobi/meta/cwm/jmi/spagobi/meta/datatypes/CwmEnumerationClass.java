package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmEnumerationClass
  extends RefClass
{
  public abstract CwmEnumeration createCwmEnumeration();
  
  public abstract CwmEnumeration createCwmEnumeration(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2);
}
