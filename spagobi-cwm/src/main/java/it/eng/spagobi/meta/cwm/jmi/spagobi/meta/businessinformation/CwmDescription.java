package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import java.util.Collection;

public abstract interface CwmDescription
  extends CwmNamespace
{
  public abstract String getBody();
  
  public abstract void setBody(String paramString);
  
  public abstract String getLanguage();
  
  public abstract void setLanguage(String paramString);
  
  public abstract String getType();
  
  public abstract void setType(String paramString);
  
  public abstract Collection getModelElement();
}
