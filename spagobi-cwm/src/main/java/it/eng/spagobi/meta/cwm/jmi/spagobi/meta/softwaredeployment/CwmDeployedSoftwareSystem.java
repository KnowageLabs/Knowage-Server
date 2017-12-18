package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;

public abstract interface CwmDeployedSoftwareSystem
  extends CwmPackage
{
  public abstract String getFixLevel();
  
  public abstract void setFixLevel(String paramString);
  
  public abstract CwmSoftwareSystem getSoftwareSystem();
  
  public abstract void setSoftwareSystem(CwmSoftwareSystem paramCwmSoftwareSystem);
}
