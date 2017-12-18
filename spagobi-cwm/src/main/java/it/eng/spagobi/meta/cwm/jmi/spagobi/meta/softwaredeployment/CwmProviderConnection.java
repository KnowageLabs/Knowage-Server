package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmProviderConnection
  extends CwmModelElement
{
  public abstract boolean isReadOnly();
  
  public abstract void setReadOnly(boolean paramBoolean);
  
  public abstract CwmDataProvider getDataProvider();
  
  public abstract void setDataProvider(CwmDataProvider paramCwmDataProvider);
  
  public abstract CwmDataManager getDataManager();
  
  public abstract void setDataManager(CwmDataManager paramCwmDataManager);
}
