package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCallActionClass
  extends RefClass
{
  public abstract CwmCallAction createCwmCallAction();
  
  public abstract CwmCallAction createCwmCallAction(String paramString, VisibilityKind paramVisibilityKind);
}
