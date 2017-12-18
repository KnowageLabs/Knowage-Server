package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmFeatureMapClass
  extends RefClass
{
  public abstract CwmFeatureMap createCwmFeatureMap();
  
  public abstract CwmFeatureMap createCwmFeatureMap(String paramString1, VisibilityKind paramVisibilityKind, CwmProcedureExpression paramCwmProcedureExpression, String paramString2);
}
