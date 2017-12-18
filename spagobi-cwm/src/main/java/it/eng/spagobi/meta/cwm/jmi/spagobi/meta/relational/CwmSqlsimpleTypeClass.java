package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqlsimpleTypeClass
  extends RefClass
{
  public abstract CwmSqlsimpleType createCwmSqlsimpleType();
  
  public abstract CwmSqlsimpleType createCwmSqlsimpleType(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, Integer paramInteger5, Integer paramInteger6, Integer paramInteger7);
}
