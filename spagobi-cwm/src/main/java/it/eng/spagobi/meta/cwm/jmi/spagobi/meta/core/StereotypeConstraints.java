package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface StereotypeConstraints
  extends RefAssociation
{
  public abstract boolean exists(CwmConstraint paramCwmConstraint, CwmStereotype paramCwmStereotype);
  
  public abstract Collection getStereotypeConstraint(CwmStereotype paramCwmStereotype);
  
  public abstract CwmStereotype getConstrainedStereotype(CwmConstraint paramCwmConstraint);
  
  public abstract boolean add(CwmConstraint paramCwmConstraint, CwmStereotype paramCwmStereotype);
  
  public abstract boolean remove(CwmConstraint paramCwmConstraint, CwmStereotype paramCwmStereotype);
}
