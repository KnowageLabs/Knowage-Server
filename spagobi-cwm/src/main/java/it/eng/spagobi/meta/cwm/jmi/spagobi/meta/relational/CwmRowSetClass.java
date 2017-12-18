package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmRowSetClass
  extends RefClass
{
  public abstract CwmRowSet createCwmRowSet();
  
  public abstract CwmRowSet createCwmRowSet(String paramString, VisibilityKind paramVisibilityKind);
}
