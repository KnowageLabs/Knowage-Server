package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTransformationStepClass
  extends RefClass
{
  public abstract CwmTransformationStep createCwmTransformationStep();
  
  public abstract CwmTransformationStep createCwmTransformationStep(String paramString, VisibilityKind paramVisibilityKind);
}
