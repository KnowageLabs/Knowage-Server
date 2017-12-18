package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ModelElementDescription
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmDescription paramCwmDescription);
  
  public abstract Collection getModelElement(CwmDescription paramCwmDescription);
  
  public abstract Collection getDescription(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmDescription paramCwmDescription);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmDescription paramCwmDescription);
}
