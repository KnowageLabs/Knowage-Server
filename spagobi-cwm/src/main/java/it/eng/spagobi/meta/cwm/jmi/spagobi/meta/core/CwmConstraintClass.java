package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmConstraintClass
  extends RefClass
{
  public abstract CwmConstraint createCwmConstraint();
  
  public abstract CwmConstraint createCwmConstraint(String paramString, VisibilityKind paramVisibilityKind, CwmBooleanExpression paramCwmBooleanExpression);
}
