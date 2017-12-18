package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DataObjectSetElement
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getElement(CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getSet(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmDataObjectSet paramCwmDataObjectSet);
}
