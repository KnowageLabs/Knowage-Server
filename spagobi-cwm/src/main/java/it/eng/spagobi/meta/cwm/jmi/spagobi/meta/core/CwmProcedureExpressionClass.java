package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmProcedureExpressionClass
  extends RefClass
{
  public abstract CwmProcedureExpression createCwmProcedureExpression();
  
  public abstract CwmProcedureExpression createCwmProcedureExpression(String paramString1, String paramString2);
}
