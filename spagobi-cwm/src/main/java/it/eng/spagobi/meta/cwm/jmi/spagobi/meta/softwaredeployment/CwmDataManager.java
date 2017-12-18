package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;

public abstract interface CwmDataManager
  extends CwmDeployedComponent
{
  public abstract boolean isCaseSensitive();
  
  public abstract void setCaseSensitive(boolean paramBoolean);
  
  public abstract Collection getDataPackage();
}
