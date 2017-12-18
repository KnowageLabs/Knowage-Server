package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions.CwmExpressionNode;

public abstract interface CwmTransformationTree
  extends CwmTransformation
{
  public abstract TreeType getType();
  
  public abstract void setType(TreeType paramTreeType);
  
  public abstract CwmExpressionNode getBody();
  
  public abstract void setBody(CwmExpressionNode paramCwmExpressionNode);
}
