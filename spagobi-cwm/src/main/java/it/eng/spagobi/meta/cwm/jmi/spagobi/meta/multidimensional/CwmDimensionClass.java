package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDimensionClass
  extends RefClass
{
  public abstract CwmDimension createCwmDimension();
  
  public abstract CwmDimension createCwmDimension(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
