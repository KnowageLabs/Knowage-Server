package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.CwmQueryExpression;

public abstract interface CwmQueryColumnSet
  extends CwmColumnSet
{
  public abstract CwmQueryExpression getQuery();
  
  public abstract void setQuery(CwmQueryExpression paramCwmQueryExpression);
}
