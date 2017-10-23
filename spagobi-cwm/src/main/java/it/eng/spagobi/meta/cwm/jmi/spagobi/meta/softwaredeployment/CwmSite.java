package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation.CwmLocation;
import java.util.Collection;

public abstract interface CwmSite
  extends CwmLocation
{
  public abstract Collection getContainingSite();
}
