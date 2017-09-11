package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationActivityClass
  extends RefClass
{
  public abstract CwmTransformationActivity createCwmTransformationActivity();
  
  public abstract CwmTransformationActivity createCwmTransformationActivity(String paramString1, VisibilityKind paramVisibilityKind, boolean paramBoolean, String paramString2);
}
