package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.List;

public abstract interface CwmCallAction
  extends CwmModelElement
{
  public abstract CwmOperation getOperation();
  
  public abstract void setOperation(CwmOperation paramCwmOperation);
  
  public abstract List getActualArgument();
}
