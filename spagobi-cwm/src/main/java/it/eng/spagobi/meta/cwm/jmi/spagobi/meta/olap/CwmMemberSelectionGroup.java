package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmMemberSelectionGroup
  extends CwmClass
{
  public abstract Collection getMemberSelection();
  
  public abstract CwmCubeRegion getCubeRegion();
  
  public abstract void setCubeRegion(CwmCubeRegion paramCwmCubeRegion);
}
