package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;

public abstract interface CwmExpressionNode
  extends CwmElement
{
  public abstract CwmExpression getExpression();
  
  public abstract void setExpression(CwmExpression paramCwmExpression);
  
  public abstract CwmFeatureNode getFeatureNode();
  
  public abstract void setFeatureNode(CwmFeatureNode paramCwmFeatureNode);
  
  public abstract CwmClassifier getType();
  
  public abstract void setType(CwmClassifier paramCwmClassifier);
}
