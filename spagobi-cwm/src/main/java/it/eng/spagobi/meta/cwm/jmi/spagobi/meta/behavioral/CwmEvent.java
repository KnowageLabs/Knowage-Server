package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.List;

public abstract interface CwmEvent
  extends CwmModelElement
{
  public abstract List getParameter();
}
