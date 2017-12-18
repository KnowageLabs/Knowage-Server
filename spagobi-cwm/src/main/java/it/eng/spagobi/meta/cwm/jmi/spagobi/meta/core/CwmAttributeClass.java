package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmAttributeClass
  extends RefClass
{
  public abstract CwmAttribute createCwmAttribute();
  
  public abstract CwmAttribute createCwmAttribute(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind1, ChangeableKind paramChangeableKind, CwmMultiplicity paramCwmMultiplicity, OrderingKind paramOrderingKind, ScopeKind paramScopeKind2, CwmExpression paramCwmExpression);
}
