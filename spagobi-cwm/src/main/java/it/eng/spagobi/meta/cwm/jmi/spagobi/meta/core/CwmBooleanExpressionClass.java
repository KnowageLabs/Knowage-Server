package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmBooleanExpressionClass
  extends RefClass
{
  public abstract CwmBooleanExpression createCwmBooleanExpression();
  
  public abstract CwmBooleanExpression createCwmBooleanExpression(String paramString1, String paramString2);
}
