package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeOwnsCubeRegions
  extends RefAssociation
{
  public abstract boolean exists(CwmCubeRegion paramCwmCubeRegion, CwmCube paramCwmCube);
  
  public abstract Collection getCubeRegion(CwmCube paramCwmCube);
  
  public abstract CwmCube getCube(CwmCubeRegion paramCwmCubeRegion);
  
  public abstract boolean add(CwmCubeRegion paramCwmCubeRegion, CwmCube paramCwmCube);
  
  public abstract boolean remove(CwmCubeRegion paramCwmCubeRegion, CwmCube paramCwmCube);
}
