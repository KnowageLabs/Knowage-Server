package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface EventParameter
  extends RefAssociation
{
  public abstract boolean exists(CwmEvent paramCwmEvent, CwmParameter paramCwmParameter);
  
  public abstract CwmEvent getEvent(CwmParameter paramCwmParameter);
  
  public abstract List getParameter(CwmEvent paramCwmEvent);
  
  public abstract boolean add(CwmEvent paramCwmEvent, CwmParameter paramCwmParameter);
  
  public abstract boolean remove(CwmEvent paramCwmEvent, CwmParameter paramCwmParameter);
}
