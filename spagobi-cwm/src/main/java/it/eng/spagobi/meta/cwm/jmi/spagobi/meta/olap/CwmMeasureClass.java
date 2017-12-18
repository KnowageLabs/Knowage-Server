package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ChangeableKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmMultiplicity;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.OrderingKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMeasureClass
  extends RefClass
{
  public abstract CwmMeasure createCwmMeasure();
  
  public abstract CwmMeasure createCwmMeasure(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind1, ChangeableKind paramChangeableKind, CwmMultiplicity paramCwmMultiplicity, OrderingKind paramOrderingKind, ScopeKind paramScopeKind2, CwmExpression paramCwmExpression);
}
