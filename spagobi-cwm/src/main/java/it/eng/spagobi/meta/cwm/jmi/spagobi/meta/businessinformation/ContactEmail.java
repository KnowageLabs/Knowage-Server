package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ContactEmail
  extends RefAssociation
{
  public abstract boolean exists(CwmEmail paramCwmEmail, CwmContact paramCwmContact);
  
  public abstract List getEmail(CwmContact paramCwmContact);
  
  public abstract Collection getContact(CwmEmail paramCwmEmail);
  
  public abstract boolean add(CwmEmail paramCwmEmail, CwmContact paramCwmContact);
  
  public abstract boolean remove(CwmEmail paramCwmEmail, CwmContact paramCwmContact);
}
