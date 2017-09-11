package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DeploymentGroupReferencesDimensionDeployments
  extends RefAssociation
{
  public abstract boolean exists(CwmDimensionDeployment paramCwmDimensionDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract Collection getDimensionDeployment(CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract CwmDeploymentGroup getDeploymentGroup(CwmDimensionDeployment paramCwmDimensionDeployment);
  
  public abstract boolean add(CwmDimensionDeployment paramCwmDimensionDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract boolean remove(CwmDimensionDeployment paramCwmDimensionDeployment, CwmDeploymentGroup paramCwmDeploymentGroup);
}
