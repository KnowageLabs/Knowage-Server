package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmFeature
  extends CwmModelElement
{
  public abstract ScopeKind getOwnerScope();
  
  public abstract void setOwnerScope(ScopeKind paramScopeKind);
  
  public abstract CwmClassifier getOwner();
  
  public abstract void setOwner(CwmClassifier paramCwmClassifier);
}
