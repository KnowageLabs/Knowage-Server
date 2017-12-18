package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes.CwmUniqueKey;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;

public abstract interface CwmUniqueConstraint
  extends CwmUniqueKey
{
  public abstract DeferrabilityType getDeferrability();
  
  public abstract void setDeferrability(DeferrabilityType paramDeferrabilityType);
}
