package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import java.util.Collection;

public abstract interface CwmClassifierMap
  extends CwmNamespace
{
  public abstract CwmProcedureExpression getFunction();
  
  public abstract void setFunction(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract String getFunctionDescription();
  
  public abstract void setFunctionDescription(String paramString);
  
  public abstract Collection getSource();
  
  public abstract Collection getTarget();
  
  public abstract CwmNamespace getTransformationMap();
  
  public abstract void setTransformationMap(CwmNamespace paramCwmNamespace);
  
  public abstract Collection getFeatureMap();
  
  public abstract Collection getCfMap();
}
