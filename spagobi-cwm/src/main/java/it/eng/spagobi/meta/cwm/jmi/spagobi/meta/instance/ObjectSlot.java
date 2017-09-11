package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ObjectSlot
  extends RefAssociation
{
  public abstract boolean exists(CwmSlot paramCwmSlot, CwmObject paramCwmObject);
  
  public abstract Collection getSlot(CwmObject paramCwmObject);
  
  public abstract CwmObject getObject(CwmSlot paramCwmSlot);
  
  public abstract boolean add(CwmSlot paramCwmSlot, CwmObject paramCwmObject);
  
  public abstract boolean remove(CwmSlot paramCwmSlot, CwmObject paramCwmObject);
}
