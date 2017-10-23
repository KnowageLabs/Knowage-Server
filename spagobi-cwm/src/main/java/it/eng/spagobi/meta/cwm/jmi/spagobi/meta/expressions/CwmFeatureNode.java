package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmFeature;
import java.util.List;

public abstract interface CwmFeatureNode
  extends CwmExpressionNode
{
  public abstract List getArgument();
  
  public abstract CwmFeature getFeature();
  
  public abstract void setFeature(CwmFeature paramCwmFeature);
}
