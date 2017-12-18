package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmRowClass
  extends RefClass
{
  public abstract CwmRow createCwmRow();
  
  public abstract CwmRow createCwmRow(String paramString, VisibilityKind paramVisibilityKind);
}
