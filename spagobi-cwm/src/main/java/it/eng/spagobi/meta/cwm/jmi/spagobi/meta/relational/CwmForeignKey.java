package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes.CwmKeyRelationship;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.DeferrabilityType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ReferentialRuleType;

public abstract interface CwmForeignKey
  extends CwmKeyRelationship
{
  public abstract ReferentialRuleType getDeleteRule();
  
  public abstract void setDeleteRule(ReferentialRuleType paramReferentialRuleType);
  
  public abstract ReferentialRuleType getUpdateRule();
  
  public abstract void setUpdateRule(ReferentialRuleType paramReferentialRuleType);
  
  public abstract DeferrabilityType getDeferrability();
  
  public abstract void setDeferrability(DeferrabilityType paramDeferrabilityType);
}
