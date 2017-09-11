package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCubeClass
  extends RefClass
{
  public abstract CwmCube createCwmCube();
  
  public abstract CwmCube createCwmCube(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2);
}
