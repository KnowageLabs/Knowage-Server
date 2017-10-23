package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmColumnSetClass
  extends RefClass
{
  public abstract CwmColumnSet createCwmColumnSet();
  
  public abstract CwmColumnSet createCwmColumnSet(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
