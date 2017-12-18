package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmSubsystem;
import java.util.Collection;

public abstract interface CwmSoftwareSystem
  extends CwmSubsystem
{
  public abstract String getType();
  
  public abstract void setType(String paramString);
  
  public abstract String getSubtype();
  
  public abstract void setSubtype(String paramString);
  
  public abstract String getSupplier();
  
  public abstract void setSupplier(String paramString);
  
  public abstract String getVersion();
  
  public abstract void setVersion(String paramString);
  
  public abstract Collection getTypespace();
}
