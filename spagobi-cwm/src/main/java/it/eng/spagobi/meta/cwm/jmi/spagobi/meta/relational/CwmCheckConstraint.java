package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmConstraint;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;

public abstract interface CwmCheckConstraint
  extends CwmConstraint
{
  public abstract DeferrabilityType getDeferrability();
  
  public abstract void setDeferrability(DeferrabilityType paramDeferrabilityType);
}
