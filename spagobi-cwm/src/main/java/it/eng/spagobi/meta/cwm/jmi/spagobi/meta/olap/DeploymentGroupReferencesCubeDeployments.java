package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DeploymentGroupReferencesCubeDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmCubeDeployment paramCwmCubeDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract Collection getCubeDeployment(CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract CwmDeploymentGroup getDeploymentGroup(CwmCubeDeployment paramCwmCubeDeployment);
  
  public abstract boolean add(CwmCubeDeployment paramCwmCubeDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract boolean remove(CwmCubeDeployment paramCwmCubeDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
}
