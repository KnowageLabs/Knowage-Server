package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface RelatedSites
  extends RefAssociation
{
  public abstract boolean exists(CwmSite paramCwmSite1, CwmSite paramCwmSite2);
  
  public abstract Collection getContainingSite(CwmSite paramCwmSite);
  
  public abstract Collection getContainedSite(CwmSite paramCwmSite);
  
  public abstract boolean add(CwmSite paramCwmSite1, CwmSite paramCwmSite2);
  
  public abstract boolean remove(CwmSite paramCwmSite1, CwmSite paramCwmSite2);
}
