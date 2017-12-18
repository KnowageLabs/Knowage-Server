package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqlindexColumnClass
  extends RefClass
{
  public abstract CwmSqlindexColumn createCwmSqlindexColumn();
  
  public abstract CwmSqlindexColumn createCwmSqlindexColumn(String paramString, VisibilityKind paramVisibilityKind, Boolean paramBoolean);
}
