package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmNamedColumnSetClass
  extends RefClass
{
  public abstract CwmNamedColumnSet createCwmNamedColumnSet();
  
  public abstract CwmNamedColumnSet createCwmNamedColumnSet(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
