package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmUniqueConstraintClass
  extends RefClass
{
  public abstract CwmUniqueConstraint createCwmUniqueConstraint();
  
  public abstract CwmUniqueConstraint createCwmUniqueConstraint(String paramString, VisibilityKind paramVisibilityKind, DeferrabilityType paramDeferrabilityType);
}
