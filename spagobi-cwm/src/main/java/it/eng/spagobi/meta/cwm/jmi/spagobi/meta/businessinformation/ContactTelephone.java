package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ContactTelephone
  extends RefAssociation
{
  public abstract boolean exists(CwmTelephone paramCwmTelephone, CwmContact paramCwmContact);
  
  public abstract List getTelephone(CwmContact paramCwmContact);
  
  public abstract Collection getContact(CwmTelephone paramCwmTelephone);
  
  public abstract boolean add(CwmTelephone paramCwmTelephone, CwmContact paramCwmContact);
  
  public abstract boolean remove(CwmTelephone paramCwmTelephone, CwmContact paramCwmContact);
}
