package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;

public abstract interface CwmEmail
  extends CwmModelElement
{
  public abstract String getEmailAddress();
  
  public abstract void setEmailAddress(String paramString);
  
  public abstract String getEmailType();
  
  public abstract void setEmailType(String paramString);
  
  public abstract Collection getContact();
}
