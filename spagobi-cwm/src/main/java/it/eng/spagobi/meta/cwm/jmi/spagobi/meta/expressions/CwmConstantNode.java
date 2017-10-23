package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

public abstract interface CwmConstantNode
  extends CwmExpressionNode
{
  public abstract String getValue();
  
  public abstract void setValue(String paramString);
}
