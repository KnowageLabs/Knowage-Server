package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDependency;

public abstract interface CwmPackageUsage
  extends CwmDependency
{
  public abstract String getPackageAlias();
  
  public abstract void setPackageAlias(String paramString);
}
