package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmLevelClass
  extends RefClass
{
  public abstract CwmLevel createCwmLevel();
  
  public abstract CwmLevel createCwmLevel(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
