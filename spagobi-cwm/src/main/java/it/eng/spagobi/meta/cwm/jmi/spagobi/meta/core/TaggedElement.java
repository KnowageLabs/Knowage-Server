package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TaggedElement
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmTaggedValue paramCwmTaggedValue);
  
  public abstract CwmModelElement getModelElement(CwmTaggedValue paramCwmTaggedValue);
  
  public abstract Collection getTaggedValue(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmTaggedValue paramCwmTaggedValue);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmTaggedValue paramCwmTaggedValue);
}
