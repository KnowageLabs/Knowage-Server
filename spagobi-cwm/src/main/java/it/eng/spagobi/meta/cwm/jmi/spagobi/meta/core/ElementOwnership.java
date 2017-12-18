package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ElementOwnership
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmNamespace paramCwmNamespace);
  
  public abstract Collection getOwnedElement(CwmNamespace paramCwmNamespace);
  
  public abstract CwmNamespace getNamespace(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmNamespace paramCwmNamespace);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmNamespace paramCwmNamespace);
}
