package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import javax.jmi.reflect.RefClass;

public abstract interface CwmQueryExpressionClass
  extends RefClass
{
  public abstract CwmQueryExpression createCwmQueryExpression();
  
  public abstract CwmQueryExpression createCwmQueryExpression(String paramString1, String paramString2);
}
