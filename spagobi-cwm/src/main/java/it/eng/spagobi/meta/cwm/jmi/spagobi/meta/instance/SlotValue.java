package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SlotValue
  extends RefAssociation
{
  public abstract boolean exists(CwmSlot paramCwmSlot, CwmInstance paramCwmInstance);
  
  public abstract Collection getValueSlot(CwmInstance paramCwmInstance);
  
  public abstract CwmInstance getValue(CwmSlot paramCwmSlot);
  
  public abstract boolean add(CwmSlot paramCwmSlot, CwmInstance paramCwmInstance);
  
  public abstract boolean remove(CwmSlot paramCwmSlot, CwmInstance paramCwmInstance);
}
