package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDataSlotClass
  extends RefClass
{
  public abstract CwmDataSlot createCwmDataSlot();
  
  public abstract CwmDataSlot createCwmDataSlot(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
