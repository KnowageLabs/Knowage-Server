package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SiteMachines
  extends RefAssociation
{
  public abstract boolean exists(CwmSite paramCwmSite, CwmMachine paramCwmMachine);
  
  public abstract CwmSite getSite(CwmMachine paramCwmMachine);
  
  public abstract Collection getMachine(CwmSite paramCwmSite);
  
  public abstract boolean add(CwmSite paramCwmSite, CwmMachine paramCwmMachine);
  
  public abstract boolean remove(CwmSite paramCwmSite, CwmMachine paramCwmMachine);
}
