package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import java.util.Collection;

public abstract interface CwmFeatureMap
  extends CwmModelElement
{
  public abstract CwmProcedureExpression getFunction();
  
  public abstract void setFunction(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract String getFunctionDescription();
  
  public abstract void setFunctionDescription(String paramString);
  
  public abstract Collection getSource();
  
  public abstract Collection getTarget();
  
  public abstract CwmClassifierMap getClassifierMap();
  
  public abstract void setClassifierMap(CwmClassifierMap paramCwmClassifierMap);
}
