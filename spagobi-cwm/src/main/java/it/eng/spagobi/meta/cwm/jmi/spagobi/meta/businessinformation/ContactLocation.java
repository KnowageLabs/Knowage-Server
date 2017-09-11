package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ContactLocation
  extends RefAssociation
{
  public abstract boolean exists(CwmLocation paramCwmLocation, CwmContact paramCwmContact);
  
  public abstract List getLocation(CwmContact paramCwmContact);
  
  public abstract Collection getContact(CwmLocation paramCwmLocation);
  
  public abstract boolean add(CwmLocation paramCwmLocation, CwmContact paramCwmContact);
  
  public abstract boolean remove(CwmLocation paramCwmLocation, CwmContact paramCwmContact);
}
