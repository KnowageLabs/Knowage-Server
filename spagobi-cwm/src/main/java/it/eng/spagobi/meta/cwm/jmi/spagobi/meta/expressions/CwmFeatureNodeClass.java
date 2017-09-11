package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmFeatureNodeClass
  extends RefClass
{
  public abstract CwmFeatureNode createCwmFeatureNode();
  
  public abstract CwmFeatureNode createCwmFeatureNode(CwmExpression paramCwmExpression);
}
