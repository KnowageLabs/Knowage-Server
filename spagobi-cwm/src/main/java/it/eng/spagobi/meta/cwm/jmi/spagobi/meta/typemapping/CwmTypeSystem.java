package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;

public abstract interface CwmTypeSystem
  extends CwmPackage
{
  public abstract String getVersion();
  
  public abstract void setVersion(String paramString);
}
