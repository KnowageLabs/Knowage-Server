package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DependencySupplier
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmDependency paramCwmDependency);
  
  public abstract Collection getSupplier(CwmDependency paramCwmDependency);
  
  public abstract Collection getSupplierDependency(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmDependency paramCwmDependency);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmDependency paramCwmDependency);
}
