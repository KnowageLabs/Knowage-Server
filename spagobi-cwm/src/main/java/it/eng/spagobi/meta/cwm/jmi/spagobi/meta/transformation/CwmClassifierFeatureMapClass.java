package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmClassifierFeatureMapClass
  extends RefClass
{
  public abstract CwmClassifierFeatureMap createCwmClassifierFeatureMap();
  
  public abstract CwmClassifierFeatureMap createCwmClassifierFeatureMap(String paramString1, VisibilityKind paramVisibilityKind, CwmProcedureExpression paramCwmProcedureExpression, String paramString2, boolean paramBoolean);
}
