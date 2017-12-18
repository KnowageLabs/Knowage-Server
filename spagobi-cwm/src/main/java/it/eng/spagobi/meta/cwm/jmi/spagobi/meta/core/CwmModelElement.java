package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmModelElement
  extends CwmElement
{
  public abstract String getName();
  
  public abstract void setName(String paramString);
  
  public abstract VisibilityKind getVisibility();
  
  public abstract void setVisibility(VisibilityKind paramVisibilityKind);
  
  public abstract Collection getClientDependency();
  
  public abstract Collection getConstraint();
  
  public abstract Collection getImporter();
  
  public abstract CwmNamespace getNamespace();
  
  public abstract void setNamespace(CwmNamespace paramCwmNamespace);
  
  public abstract Collection getTaggedValue();
}
