package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ReferencedElement
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmElementNode paramCwmElementNode);
  
  public abstract CwmModelElement getModelElement(CwmElementNode paramCwmElementNode);
  
  public abstract Collection getElementNode(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmElementNode paramCwmElementNode);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmElementNode paramCwmElementNode);
}
