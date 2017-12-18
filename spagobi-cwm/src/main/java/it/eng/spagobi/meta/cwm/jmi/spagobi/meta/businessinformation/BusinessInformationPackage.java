package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import javax.jmi.reflect.RefPackage;

public abstract interface BusinessInformationPackage
  extends RefPackage
{
  public abstract CwmResponsiblePartyClass getCwmResponsibleParty();
  
  public abstract CwmTelephoneClass getCwmTelephone();
  
  public abstract CwmEmailClass getCwmEmail();
  
  public abstract CwmLocationClass getCwmLocation();
  
  public abstract CwmContactClass getCwmContact();
  
  public abstract CwmDescriptionClass getCwmDescription();
  
  public abstract CwmDocumentClass getCwmDocument();
  
  public abstract CwmResourceLocatorClass getCwmResourceLocator();
  
  public abstract ContactEmail getContactEmail();
  
  public abstract ContactLocation getContactLocation();
  
  public abstract ContactResourceLocator getContactResourceLocator();
  
  public abstract ContactTelephone getContactTelephone();
  
  public abstract DocumentDescribes getDocumentDescribes();
  
  public abstract ModelElementDescription getModelElementDescription();
  
  public abstract ModelElementResponsibility getModelElementResponsibility();
  
  public abstract ResponsiblePartyContact getResponsiblePartyContact();
}
