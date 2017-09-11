package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ContactResourceLocator
  extends RefAssociation
{
  public abstract boolean exists(CwmResourceLocator paramCwmResourceLocator, CwmContact paramCwmContact);
  
  public abstract List getUrl(CwmContact paramCwmContact);
  
  public abstract Collection getContact(CwmResourceLocator paramCwmResourceLocator);
  
  public abstract boolean add(CwmResourceLocator paramCwmResourceLocator, CwmContact paramCwmContact);
  
  public abstract boolean remove(CwmResourceLocator paramCwmResourceLocator, CwmContact paramCwmContact);
}
