package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmPrimaryKeyClass
  extends RefClass
{
  public abstract CwmPrimaryKey createCwmPrimaryKey();
  
  public abstract CwmPrimaryKey createCwmPrimaryKey(String paramString, VisibilityKind paramVisibilityKind, DeferrabilityType paramDeferrabilityType);
}
