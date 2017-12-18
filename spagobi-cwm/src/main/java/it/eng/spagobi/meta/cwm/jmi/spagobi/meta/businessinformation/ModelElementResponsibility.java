package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ModelElementResponsibility
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract Collection getModelElement(CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract Collection getResponsibleParty(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmResponsibleParty paramCwmResponsibleParty);
}
