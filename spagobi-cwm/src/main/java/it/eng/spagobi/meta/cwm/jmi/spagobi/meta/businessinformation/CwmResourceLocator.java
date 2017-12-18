package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;

public abstract interface CwmResourceLocator
  extends CwmModelElement
{
  public abstract String getUrl();
  
  public abstract void setUrl(String paramString);
  
  public abstract Collection getContact();
}
