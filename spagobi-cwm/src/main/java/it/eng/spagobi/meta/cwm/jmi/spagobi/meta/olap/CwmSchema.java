package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;

public abstract interface CwmSchema
  extends CwmPackage
{
  public abstract Collection getCube();
  
  public abstract Collection getDimension();
  
  public abstract Collection getDeploymentGroup();
}
