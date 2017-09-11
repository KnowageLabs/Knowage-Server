package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;

public abstract interface CwmCatalog
  extends CwmPackage
{
  public abstract String getDefaultCharacterSetName();
  
  public abstract void setDefaultCharacterSetName(String paramString);
  
  public abstract String getDefaultCollationName();
  
  public abstract void setDefaultCollationName(String paramString);
}
