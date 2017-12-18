package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface StereotypedElement
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmStereotype paramCwmStereotype);
  
  public abstract Collection getExtendedElement(CwmStereotype paramCwmStereotype);
  
  public abstract CwmStereotype getStereotype(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmStereotype paramCwmStereotype);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmStereotype paramCwmStereotype);
}
