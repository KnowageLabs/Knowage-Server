package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.List;

public abstract interface CwmConstraint
  extends CwmModelElement
{
  public abstract CwmBooleanExpression getBody();
  
  public abstract void setBody(CwmBooleanExpression paramCwmBooleanExpression);
  
  public abstract List getConstrainedElement();
}
