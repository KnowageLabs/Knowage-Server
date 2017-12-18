package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import java.util.Collection;

public abstract interface CwmTransformation
  extends CwmNamespace
{
  public abstract CwmProcedureExpression getFunction();
  
  public abstract void setFunction(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract String getFunctionDescription();
  
  public abstract void setFunctionDescription(String paramString);
  
  public abstract boolean isPrimary();
  
  public abstract void setPrimary(boolean paramBoolean);
  
  public abstract Collection getSource();
  
  public abstract Collection getTarget();
  
  public abstract Collection getUse();
}
