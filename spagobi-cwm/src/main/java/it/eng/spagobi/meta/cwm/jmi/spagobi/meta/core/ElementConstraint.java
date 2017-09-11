package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ElementConstraint
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmConstraint paramCwmConstraint);
  
  public abstract List getConstrainedElement(CwmConstraint paramCwmConstraint);
  
  public abstract Collection getConstraint(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmConstraint paramCwmConstraint);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmConstraint paramCwmConstraint);
}
