package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmElementNodeClass
  extends RefClass
{
  public abstract CwmElementNode createCwmElementNode();
  
  public abstract CwmElementNode createCwmElementNode(CwmExpression paramCwmExpression);
}
