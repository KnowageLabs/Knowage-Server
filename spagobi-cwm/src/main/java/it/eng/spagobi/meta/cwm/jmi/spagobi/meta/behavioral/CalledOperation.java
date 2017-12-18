package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CalledOperation
  extends RefAssociation
{
  public abstract boolean exists(CwmCallAction paramCwmCallAction, CwmOperation paramCwmOperation);
  
  public abstract Collection getCallAction(CwmOperation paramCwmOperation);
  
  public abstract CwmOperation getOperation(CwmCallAction paramCwmCallAction);
  
  public abstract boolean add(CwmCallAction paramCwmCallAction, CwmOperation paramCwmOperation);
  
  public abstract boolean remove(CwmCallAction paramCwmCallAction, CwmOperation paramCwmOperation);
}
