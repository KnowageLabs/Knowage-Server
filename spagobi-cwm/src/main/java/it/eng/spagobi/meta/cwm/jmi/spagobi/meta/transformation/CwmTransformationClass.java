package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationClass
  extends RefClass
{
  public abstract CwmTransformation createCwmTransformation();
  
  public abstract CwmTransformation createCwmTransformation(String paramString1, VisibilityKind paramVisibilityKind, CwmProcedureExpression paramCwmProcedureExpression, String paramString2, boolean paramBoolean);
}
