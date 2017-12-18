package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface OperationArgument
  extends RefAssociation
{
  public abstract boolean exists(CwmFeatureNode paramCwmFeatureNode, CwmExpressionNode paramCwmExpressionNode);
  
  public abstract CwmFeatureNode getFeatureNode(CwmExpressionNode paramCwmExpressionNode);
  
  public abstract List getArgument(CwmFeatureNode paramCwmFeatureNode);
  
  public abstract boolean add(CwmFeatureNode paramCwmFeatureNode, CwmExpressionNode paramCwmExpressionNode);
  
  public abstract boolean remove(CwmFeatureNode paramCwmFeatureNode, CwmExpressionNode paramCwmExpressionNode);
}
