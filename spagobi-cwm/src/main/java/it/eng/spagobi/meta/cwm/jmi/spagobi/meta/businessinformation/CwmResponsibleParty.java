package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import java.util.Collection;
import java.util.List;

public abstract interface CwmResponsibleParty
  extends CwmNamespace
{
  public abstract String getResponsibility();
  
  public abstract void setResponsibility(String paramString);
  
  public abstract List getContact();
  
  public abstract Collection getModelElement();
}
