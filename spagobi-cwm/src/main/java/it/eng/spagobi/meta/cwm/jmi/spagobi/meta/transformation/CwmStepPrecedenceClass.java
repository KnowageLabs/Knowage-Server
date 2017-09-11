package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmStepPrecedenceClass
  extends RefClass
{
  public abstract CwmStepPrecedence createCwmStepPrecedence();
  
  public abstract CwmStepPrecedence createCwmStepPrecedence(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
