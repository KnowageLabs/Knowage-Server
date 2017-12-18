package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqldistinctTypeClass
  extends RefClass
{
  public abstract CwmSqldistinctType createCwmSqldistinctType();
  
  public abstract CwmSqldistinctType createCwmSqldistinctType(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4);
}
