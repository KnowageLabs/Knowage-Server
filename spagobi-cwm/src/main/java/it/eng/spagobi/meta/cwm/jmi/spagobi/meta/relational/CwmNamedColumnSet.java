package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;

public abstract interface CwmNamedColumnSet
  extends CwmColumnSet
{
  public abstract Collection getOptionScopeColumn();
  
  public abstract CwmSqlstructuredType getType();
  
  public abstract void setType(CwmSqlstructuredType paramCwmSqlstructuredType);
  
  public abstract Collection getUsingTrigger();
}
