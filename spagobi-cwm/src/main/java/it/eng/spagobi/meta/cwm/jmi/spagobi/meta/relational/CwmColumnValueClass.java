package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmColumnValueClass
  extends RefClass
{
  public abstract CwmColumnValue createCwmColumnValue();
  
  public abstract CwmColumnValue createCwmColumnValue(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
