package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmExpressionNodeClass
  extends RefClass
{
  public abstract CwmExpressionNode createCwmExpressionNode();
  
  public abstract CwmExpressionNode createCwmExpressionNode(CwmExpression paramCwmExpression);
}
