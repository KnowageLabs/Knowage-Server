package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DataProviderConnections
  extends RefAssociation
{
  public abstract boolean exists(CwmDataProvider paramCwmDataProvider, CwmProviderConnection paramCwmProviderConnection);
  
  public abstract CwmDataProvider getDataProvider(CwmProviderConnection paramCwmProviderConnection);
  
  public abstract Collection getResourceConnection(CwmDataProvider paramCwmDataProvider);
  
  public abstract boolean add(CwmDataProvider paramCwmDataProvider, CwmProviderConnection paramCwmProviderConnection);
  
  public abstract boolean remove(CwmDataProvider paramCwmDataProvider, CwmProviderConnection paramCwmProviderConnection);
}
