package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;

public abstract interface CwmDataProvider
  extends CwmDataManager
{
  public abstract Collection getResourceConnection();
}
