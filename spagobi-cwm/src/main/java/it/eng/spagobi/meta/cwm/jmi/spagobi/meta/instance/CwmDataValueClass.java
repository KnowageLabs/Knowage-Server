package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDataValueClass
  extends RefClass
{
  public abstract CwmDataValue createCwmDataValue();
  
  public abstract CwmDataValue createCwmDataValue(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
