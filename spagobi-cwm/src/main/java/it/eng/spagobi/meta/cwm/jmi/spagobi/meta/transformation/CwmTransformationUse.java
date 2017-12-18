package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDependency;
import java.util.Collection;

public abstract interface CwmTransformationUse
  extends CwmDependency
{
  public abstract String getType();
  
  public abstract void setType(String paramString);
  
  public abstract Collection getTransformation();
  
  public abstract Collection getOperation();
}
