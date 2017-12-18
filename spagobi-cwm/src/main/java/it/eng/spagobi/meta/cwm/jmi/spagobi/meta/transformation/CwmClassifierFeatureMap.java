package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import java.util.Collection;

public abstract interface CwmClassifierFeatureMap
  extends CwmModelElement
{
  public abstract CwmProcedureExpression getFunction();
  
  public abstract void setFunction(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract String getFunctionDescription();
  
  public abstract void setFunctionDescription(String paramString);
  
  public abstract boolean isClassifierToFeature();
  
  public abstract void setClassifierToFeature(boolean paramBoolean);
  
  public abstract Collection getClassifier();
  
  public abstract Collection getFeature();
  
  public abstract CwmClassifierMap getClassifierMap();
  
  public abstract void setClassifierMap(CwmClassifierMap paramCwmClassifierMap);
}
