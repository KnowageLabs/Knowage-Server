package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import javax.jmi.reflect.RefPackage;

public abstract interface ExpressionsPackage
  extends RefPackage
{
  public abstract CwmExpressionNodeClass getCwmExpressionNode();
  
  public abstract CwmConstantNodeClass getCwmConstantNode();
  
  public abstract CwmElementNodeClass getCwmElementNode();
  
  public abstract CwmFeatureNodeClass getCwmFeatureNode();
  
  public abstract ExpressionNodeClassifier getExpressionNodeClassifier();
  
  public abstract NodeFeature getNodeFeature();
  
  public abstract OperationArgument getOperationArgument();
  
  public abstract ReferencedElement getReferencedElement();
}
