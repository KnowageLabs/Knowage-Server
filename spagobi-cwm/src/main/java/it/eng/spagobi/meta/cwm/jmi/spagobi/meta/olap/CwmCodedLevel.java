package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions.CwmExpressionNode;

public abstract interface CwmCodedLevel
  extends CwmLevel
{
  public abstract CwmExpressionNode getEncoding();
  
  public abstract void setEncoding(CwmExpressionNode paramCwmExpressionNode);
}
