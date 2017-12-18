package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmStructuralFeature
  extends CwmFeature
{
  public abstract ChangeableKind getChangeability();
  
  public abstract void setChangeability(ChangeableKind paramChangeableKind);
  
  public abstract CwmMultiplicity getMultiplicity();
  
  public abstract void setMultiplicity(CwmMultiplicity paramCwmMultiplicity);
  
  public abstract OrderingKind getOrdering();
  
  public abstract void setOrdering(OrderingKind paramOrderingKind);
  
  public abstract ScopeKind getTargetScope();
  
  public abstract void setTargetScope(ScopeKind paramScopeKind);
  
  public abstract CwmClassifier getType();
  
  public abstract void setType(CwmClassifier paramCwmClassifier);
}
