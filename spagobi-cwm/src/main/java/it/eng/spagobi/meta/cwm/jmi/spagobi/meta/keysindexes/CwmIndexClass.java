package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmIndexClass
  extends RefClass
{
  public abstract CwmIndex createCwmIndex();
  
  public abstract CwmIndex createCwmIndex(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3);
}
