package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface OperationMethod
  extends RefAssociation
{
  public abstract boolean exists(CwmOperation paramCwmOperation, CwmMethod paramCwmMethod);
  
  public abstract CwmOperation getSpecification(CwmMethod paramCwmMethod);
  
  public abstract Collection getMethod(CwmOperation paramCwmOperation);
  
  public abstract boolean add(CwmOperation paramCwmOperation, CwmMethod paramCwmMethod);
  
  public abstract boolean remove(CwmOperation paramCwmOperation, CwmMethod paramCwmMethod);
}
