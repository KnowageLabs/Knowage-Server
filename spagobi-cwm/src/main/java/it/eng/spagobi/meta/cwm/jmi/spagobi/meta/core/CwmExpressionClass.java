package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmExpressionClass
  extends RefClass
{
  public abstract CwmExpression createCwmExpression();
  
  public abstract CwmExpression createCwmExpression(String paramString1, String paramString2);
}
