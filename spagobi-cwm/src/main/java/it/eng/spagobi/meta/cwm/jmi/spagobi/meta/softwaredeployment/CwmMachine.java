package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import java.util.Collection;
import java.util.List;

public abstract interface CwmMachine
  extends CwmNamespace
{
  public abstract List getIpAddress();
  
  public abstract List getHostName();
  
  public abstract String getMachineId();
  
  public abstract void setMachineId(String paramString);
  
  public abstract CwmSite getSite();
  
  public abstract void setSite(CwmSite paramCwmSite);
  
  public abstract Collection getDeployedComponent();
}
