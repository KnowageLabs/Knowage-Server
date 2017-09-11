package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDimensionClass
  extends RefClass
{
  public abstract CwmDimension createCwmDimension();
  
  public abstract CwmDimension createCwmDimension(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3);
}
