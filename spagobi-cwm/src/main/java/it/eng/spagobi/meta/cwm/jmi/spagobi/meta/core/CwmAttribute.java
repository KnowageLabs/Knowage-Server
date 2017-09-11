package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmAttribute
  extends CwmStructuralFeature
{
  public abstract CwmExpression getInitialValue();
  
  public abstract void setInitialValue(CwmExpression paramCwmExpression);
}
