package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ResponsiblePartyContact
  extends RefAssociation
{
  public abstract boolean exists(CwmContact paramCwmContact, CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract List getContact(CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract Collection getResponsibleParty(CwmContact paramCwmContact);
  
  public abstract boolean add(CwmContact paramCwmContact, CwmResponsibleParty paramCwmResponsibleParty);
  
  public abstract boolean remove(CwmContact paramCwmContact, CwmResponsibleParty paramCwmResponsibleParty);
}
