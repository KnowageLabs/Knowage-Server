package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSlotClass
  extends RefClass
{
  public abstract CwmSlot createCwmSlot();
  
  public abstract CwmSlot createCwmSlot(String paramString, VisibilityKind paramVisibilityKind);
}
