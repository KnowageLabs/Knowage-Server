package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmInterfaceClass
  extends RefClass
{
  public abstract CwmInterface createCwmInterface();
  
  public abstract CwmInterface createCwmInterface(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
