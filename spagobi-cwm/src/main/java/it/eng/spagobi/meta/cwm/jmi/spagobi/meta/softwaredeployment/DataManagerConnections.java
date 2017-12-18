package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DataManagerConnections
  extends RefAssociation
{
  public abstract boolean exists(CwmDataManager paramCwmDataManager, CwmProviderConnection paramCwmProviderConnection);
  
  public abstract CwmDataManager getDataManager(CwmProviderConnection paramCwmProviderConnection);
  
  public abstract Collection getClientConnection(CwmDataManager paramCwmDataManager);
  
  public abstract boolean add(CwmDataManager paramCwmDataManager, CwmProviderConnection paramCwmProviderConnection);
  
  public abstract boolean remove(CwmDataManager paramCwmDataManager, CwmProviderConnection paramCwmProviderConnection);
}
