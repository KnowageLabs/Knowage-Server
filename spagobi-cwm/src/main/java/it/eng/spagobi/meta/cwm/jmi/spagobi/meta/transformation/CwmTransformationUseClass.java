package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationUseClass
  extends RefClass
{
  public abstract CwmTransformationUse createCwmTransformationUse();
  
  public abstract CwmTransformationUse createCwmTransformationUse(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
