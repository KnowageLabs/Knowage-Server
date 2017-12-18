package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmElementNode
  extends CwmExpressionNode
{
  public abstract CwmModelElement getModelElement();
  
  public abstract void setModelElement(CwmModelElement paramCwmModelElement);
}
