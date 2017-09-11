package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmArgument
  extends CwmModelElement
{
  public abstract CwmExpression getValue();
  
  public abstract void setValue(CwmExpression paramCwmExpression);
  
  public abstract CwmCallAction getCallAction();
  
  public abstract void setCallAction(CwmCallAction paramCwmCallAction);
}
