package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes.CwmIndex;

public abstract interface CwmSqlindex
  extends CwmIndex
{
  public abstract String getFilterCondition();
  
  public abstract void setFilterCondition(String paramString);
  
  public abstract boolean isNullable();
  
  public abstract void setNullable(boolean paramBoolean);
  
  public abstract boolean isAutoUpdate();
  
  public abstract void setAutoUpdate(boolean paramBoolean);
}
