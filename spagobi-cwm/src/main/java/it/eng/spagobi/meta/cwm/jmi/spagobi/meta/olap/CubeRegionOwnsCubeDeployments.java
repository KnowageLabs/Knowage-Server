package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeRegionOwnsCubeDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmCubeDeployment paramCwmCubeDeployment, CwmCubeRegion paramCwmCubeRegion);
  
  public abstract List getCubeDeployment(CwmCubeRegion paramCwmCubeRegion);
  
  public abstract CwmCubeRegion getCubeRegion(CwmCubeDeployment paramCwmCubeDeployment);
  
  public abstract boolean add(CwmCubeDeployment paramCwmCubeDeployment, CwmCubeRegion paramCwmCubeRegion);
  
  public abstract boolean remove(CwmCubeDeployment paramCwmCubeDeployment, CwmCubeRegion paramCwmCubeRegion);
}
