package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeDeploymentOwnsContentMaps
  extends RefAssociation
{
  public abstract boolean exists(CwmContentMap paramCwmContentMap, CwmCubeDeployment paramCwmCubeDeployment);
  
  public abstract Collection getContentMap(CwmCubeDeployment paramCwmCubeDeployment);
  
  public abstract CwmCubeDeployment getCubeDeployment(CwmContentMap paramCwmContentMap);
  
  public abstract boolean add(CwmContentMap paramCwmContentMap, CwmCubeDeployment paramCwmCubeDeployment);
  
  public abstract boolean remove(CwmContentMap paramCwmContentMap, CwmCubeDeployment paramCwmCubeDeployment);
}
