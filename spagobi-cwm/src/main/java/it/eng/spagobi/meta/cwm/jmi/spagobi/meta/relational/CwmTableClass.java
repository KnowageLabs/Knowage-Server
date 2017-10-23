package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTableClass
  extends RefClass
{
  public abstract CwmTable createCwmTable();
  
  public abstract CwmTable createCwmTable(String paramString1, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2, String paramString2, boolean paramBoolean3);
}
