package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ExpressionNodeClassifier
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmExpressionNode paramCwmExpressionNode);
  
  public abstract CwmClassifier getType(CwmExpressionNode paramCwmExpressionNode);
  
  public abstract Collection getExpressionNode(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmExpressionNode paramCwmExpressionNode);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmExpressionNode paramCwmExpressionNode);
}
