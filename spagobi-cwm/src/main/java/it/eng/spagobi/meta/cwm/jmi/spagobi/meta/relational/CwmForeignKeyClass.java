package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ReferentialRuleType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmForeignKeyClass
  extends RefClass
{
  public abstract CwmForeignKey createCwmForeignKey();
  
  public abstract CwmForeignKey createCwmForeignKey(String paramString, VisibilityKind paramVisibilityKind, ReferentialRuleType paramReferentialRuleType1, ReferentialRuleType paramReferentialRuleType2, DeferrabilityType paramDeferrabilityType);
}
