package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.CwmQueryExpression;

public abstract interface CwmView
  extends CwmNamedColumnSet
{
  public abstract boolean isReadOnly();
  
  public abstract void setReadOnly(boolean paramBoolean);
  
  public abstract boolean isCheckOption();
  
  public abstract void setCheckOption(boolean paramBoolean);
  
  public abstract CwmQueryExpression getQueryExpression();
  
  public abstract void setQueryExpression(CwmQueryExpression paramCwmQueryExpression);
}
