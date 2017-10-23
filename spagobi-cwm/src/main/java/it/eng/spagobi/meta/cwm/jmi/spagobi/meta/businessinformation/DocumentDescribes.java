package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DocumentDescribes
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmDocument paramCwmDocument);
  
  public abstract Collection getModelElement(CwmDocument paramCwmDocument);
  
  public abstract Collection getDocument(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmDocument paramCwmDocument);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmDocument paramCwmDocument);
}
