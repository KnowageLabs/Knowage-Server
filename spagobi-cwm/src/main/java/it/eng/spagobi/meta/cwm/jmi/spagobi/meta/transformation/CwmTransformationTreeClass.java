package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions.CwmExpressionNode;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationTreeClass
  extends RefClass
{
  public abstract CwmTransformationTree createCwmTransformationTree();
  
  public abstract CwmTransformationTree createCwmTransformationTree(String paramString1, VisibilityKind paramVisibilityKind, CwmProcedureExpression paramCwmProcedureExpression, String paramString2, boolean paramBoolean, TreeType paramTreeType, CwmExpressionNode paramCwmExpressionNode);
}
