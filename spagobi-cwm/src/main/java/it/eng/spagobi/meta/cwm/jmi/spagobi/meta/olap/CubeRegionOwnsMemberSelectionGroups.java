package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeRegionOwnsMemberSelectionGroups
  extends RefAssociation
{
  public abstract boolean exists(CwmMemberSelectionGroup paramCwmMemberSelectionGroup, CwmCubeRegion paramCwmCubeRegion);
  
  public abstract Collection getMemberSelectionGroup(CwmCubeRegion paramCwmCubeRegion);
  
  public abstract CwmCubeRegion getCubeRegion(CwmMemberSelectionGroup paramCwmMemberSelectionGroup);
  
  public abstract boolean add(CwmMemberSelectionGroup paramCwmMemberSelectionGroup, CwmCubeRegion paramCwmCubeRegion);
  
  public abstract boolean remove(CwmMemberSelectionGroup paramCwmMemberSelectionGroup, CwmCubeRegion paramCwmCubeRegion);
}
