package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmObjectClass
  extends RefClass
{
  public abstract CwmObject createCwmObject();
  
  public abstract CwmObject createCwmObject(String paramString, VisibilityKind paramVisibilityKind);
}
