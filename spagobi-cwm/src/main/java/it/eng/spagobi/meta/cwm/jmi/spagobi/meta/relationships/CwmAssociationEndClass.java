package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ChangeableKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmMultiplicity;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.OrderingKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmAssociationEndClass
  extends RefClass
{
  public abstract CwmAssociationEnd createCwmAssociationEnd();
  
  public abstract CwmAssociationEnd createCwmAssociationEnd(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind1, ChangeableKind paramChangeableKind, CwmMultiplicity paramCwmMultiplicity, OrderingKind paramOrderingKind, ScopeKind paramScopeKind2, AggregationKind paramAggregationKind, boolean paramBoolean);
}
