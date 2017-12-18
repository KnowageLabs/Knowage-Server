package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;

public abstract interface CwmMethod
  extends CwmBehavioralFeature
{
  public abstract CwmProcedureExpression getBody();
  
  public abstract void setBody(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract CwmOperation getSpecification();
  
  public abstract void setSpecification(CwmOperation paramCwmOperation);
}
