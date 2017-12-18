package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmConstantNodeClass
  extends RefClass
{
  public abstract CwmConstantNode createCwmConstantNode();
  
  public abstract CwmConstantNode createCwmConstantNode(CwmExpression paramCwmExpression, String paramString);
}
