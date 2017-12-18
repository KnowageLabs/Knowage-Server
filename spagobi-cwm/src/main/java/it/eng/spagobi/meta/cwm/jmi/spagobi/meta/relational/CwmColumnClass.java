package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ChangeableKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmMultiplicity;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.OrderingKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.NullableType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmColumnClass
  extends RefClass
{
  public abstract CwmColumn createCwmColumn();
  
  public abstract CwmColumn createCwmColumn(String paramString1, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind1, ChangeableKind paramChangeableKind, CwmMultiplicity paramCwmMultiplicity, OrderingKind paramOrderingKind, ScopeKind paramScopeKind2, CwmExpression paramCwmExpression, Integer paramInteger1, Integer paramInteger2, NullableType paramNullableType, Integer paramInteger3, String paramString2, String paramString3);
}
