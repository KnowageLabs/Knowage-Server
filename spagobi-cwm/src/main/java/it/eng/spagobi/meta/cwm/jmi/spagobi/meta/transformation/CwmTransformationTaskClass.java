package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationTaskClass
  extends RefClass
{
  public abstract CwmTransformationTask createCwmTransformationTask();
  
  public abstract CwmTransformationTask createCwmTransformationTask(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
