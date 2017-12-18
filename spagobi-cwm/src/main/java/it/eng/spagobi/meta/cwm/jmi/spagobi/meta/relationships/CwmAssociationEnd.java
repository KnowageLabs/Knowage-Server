package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;

public abstract interface CwmAssociationEnd
  extends CwmStructuralFeature
{
  public abstract AggregationKind getAggregation();
  
  public abstract void setAggregation(AggregationKind paramAggregationKind);
  
  public abstract boolean isNavigable();
  
  public abstract void setNavigable(boolean paramBoolean);
}
