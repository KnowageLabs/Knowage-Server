package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqlindexClass
  extends RefClass
{
  public abstract CwmSqlindex createCwmSqlindex();
  
  public abstract CwmSqlindex createCwmSqlindex(String paramString1, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, String paramString2, boolean paramBoolean4, boolean paramBoolean5);
}
