package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;

public abstract interface CwmDeployedComponent
  extends CwmPackage
{
  public abstract String getPathname();
  
  public abstract void setPathname(String paramString);
  
  public abstract CwmComponent getComponent();
  
  public abstract void setComponent(CwmComponent paramCwmComponent);
  
  public abstract CwmMachine getMachine();
  
  public abstract void setMachine(CwmMachine paramCwmMachine);
  
  public abstract Collection getUsingComponents();
  
  public abstract Collection getUsedComponents();
}
