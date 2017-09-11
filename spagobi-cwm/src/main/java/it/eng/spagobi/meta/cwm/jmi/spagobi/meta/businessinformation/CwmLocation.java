package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;

public abstract interface CwmLocation
  extends CwmModelElement
{
  public abstract String getLocationType();
  
  public abstract void setLocationType(String paramString);
  
  public abstract String getAddress();
  
  public abstract void setAddress(String paramString);
  
  public abstract String getCity();
  
  public abstract void setCity(String paramString);
  
  public abstract String getPostCode();
  
  public abstract void setPostCode(String paramString);
  
  public abstract String getArea();
  
  public abstract void setArea(String paramString);
  
  public abstract String getCountry();
  
  public abstract void setCountry(String paramString);
  
  public abstract Collection getContact();
}
