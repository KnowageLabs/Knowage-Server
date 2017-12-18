package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmExtentClass
  extends RefClass
{
  public abstract CwmExtent createCwmExtent();
  
  public abstract CwmExtent createCwmExtent(String paramString, VisibilityKind paramVisibilityKind);
}
