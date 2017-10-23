package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmBooleanExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmPrecedenceConstraintClass
  extends RefClass
{
  public abstract CwmPrecedenceConstraint createCwmPrecedenceConstraint();
  
  public abstract CwmPrecedenceConstraint createCwmPrecedenceConstraint(String paramString, VisibilityKind paramVisibilityKind, CwmBooleanExpression paramCwmBooleanExpression);
}
