package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmBooleanExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCheckConstraintClass
  extends RefClass
{
  public abstract CwmCheckConstraint createCwmCheckConstraint();
  
  public abstract CwmCheckConstraint createCwmCheckConstraint(String paramString, VisibilityKind paramVisibilityKind, CwmBooleanExpression paramCwmBooleanExpression, DeferrabilityType paramDeferrabilityType);
}
