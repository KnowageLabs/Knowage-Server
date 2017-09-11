package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface StereotypeTaggedValues
  extends RefAssociation
{
  public abstract boolean exists(CwmTaggedValue paramCwmTaggedValue, CwmStereotype paramCwmStereotype);
  
  public abstract Collection getRequiredTag(CwmStereotype paramCwmStereotype);
  
  public abstract CwmStereotype getStereotype(CwmTaggedValue paramCwmTaggedValue);
  
  public abstract boolean add(CwmTaggedValue paramCwmTaggedValue, CwmStereotype paramCwmStereotype);
  
  public abstract boolean remove(CwmTaggedValue paramCwmTaggedValue, CwmStereotype paramCwmStereotype);
}
