package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;
import java.util.List;

public abstract interface CwmCubeRegion
  extends CwmClass
{
  public abstract boolean isReadOnly();
  
  public abstract void setReadOnly(boolean paramBoolean);
  
  public abstract boolean isFullyRealized();
  
  public abstract void setFullyRealized(boolean paramBoolean);
  
  public abstract Collection getMemberSelectionGroup();
  
  public abstract CwmCube getCube();
  
  public abstract void setCube(CwmCube paramCwmCube);
  
  public abstract List getCubeDeployment();
}
