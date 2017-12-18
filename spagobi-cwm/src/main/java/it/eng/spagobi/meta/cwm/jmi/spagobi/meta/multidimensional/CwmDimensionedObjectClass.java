package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ChangeableKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmMultiplicity;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.OrderingKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDimensionedObjectClass
  extends RefClass
{
  public abstract CwmDimensionedObject createCwmDimensionedObject();
  
  public abstract CwmDimensionedObject createCwmDimensionedObject(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind1, ChangeableKind paramChangeableKind, CwmMultiplicity paramCwmMultiplicity, OrderingKind paramOrderingKind, ScopeKind paramScopeKind2, CwmExpression paramCwmExpression);
}
