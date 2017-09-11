package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSchemaClass
  extends RefClass
{
  public abstract CwmSchema createCwmSchema();
  
  public abstract CwmSchema createCwmSchema(String paramString, VisibilityKind paramVisibilityKind);
}
