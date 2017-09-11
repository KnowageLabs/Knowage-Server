package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqlstructuredTypeClass
  extends RefClass
{
  public abstract CwmSqlstructuredType createCwmSqlstructuredType();
  
  public abstract CwmSqlstructuredType createCwmSqlstructuredType(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean, Integer paramInteger);
}
