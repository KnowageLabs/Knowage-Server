package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmUniqueKeyClass
  extends RefClass
{
  public abstract CwmUniqueKey createCwmUniqueKey();
  
  public abstract CwmUniqueKey createCwmUniqueKey(String paramString, VisibilityKind paramVisibilityKind);
}
