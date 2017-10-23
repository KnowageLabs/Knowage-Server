package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import java.util.List;

public abstract interface CwmContact
  extends CwmModelElement
{
  public abstract List getEmail();
  
  public abstract List getLocation();
  
  public abstract Collection getResponsibleParty();
  
  public abstract List getTelephone();
  
  public abstract List getUrl();
}
