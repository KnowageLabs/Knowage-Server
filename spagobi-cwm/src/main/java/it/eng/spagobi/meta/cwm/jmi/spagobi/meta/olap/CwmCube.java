package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmCube
  extends CwmClass
{
  public abstract boolean isVirtual();
  
  public abstract void setVirtual(boolean paramBoolean);
  
  public abstract Collection getCubeDimensionAssociation();
  
  public abstract Collection getCubeRegion();
  
  public abstract CwmSchema getSchema();
  
  public abstract void setSchema(CwmSchema paramCwmSchema);
}
