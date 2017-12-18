package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmEventClass
  extends RefClass
{
  public abstract CwmEvent createCwmEvent();
  
  public abstract CwmEvent createCwmEvent(String paramString, VisibilityKind paramVisibilityKind);
}
