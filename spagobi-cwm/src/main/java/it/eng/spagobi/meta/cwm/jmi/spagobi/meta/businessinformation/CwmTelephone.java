package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;

public abstract interface CwmTelephone
  extends CwmModelElement
{
  public abstract String getPhoneNumber();
  
  public abstract void setPhoneNumber(String paramString);
  
  public abstract String getPhoneType();
  
  public abstract void setPhoneType(String paramString);
  
  public abstract Collection getContact();
}
