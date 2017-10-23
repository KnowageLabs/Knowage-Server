package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface CallArguments
  extends RefAssociation
{
  public abstract boolean exists(CwmArgument paramCwmArgument, CwmCallAction paramCwmCallAction);
  
  public abstract List getActualArgument(CwmCallAction paramCwmCallAction);
  
  public abstract CwmCallAction getCallAction(CwmArgument paramCwmArgument);
  
  public abstract boolean add(CwmArgument paramCwmArgument, CwmCallAction paramCwmCallAction);
  
  public abstract boolean remove(CwmArgument paramCwmArgument, CwmCallAction paramCwmCallAction);
}
