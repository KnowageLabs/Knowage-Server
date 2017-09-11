package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;

public abstract interface CwmDeploymentGroup
  extends CwmPackage
{
  public abstract CwmSchema getSchema();
  
  public abstract void setSchema(CwmSchema paramCwmSchema);
  
  public abstract Collection getCubeDeployment();
  
  public abstract Collection getDimensionDeployment();
}
