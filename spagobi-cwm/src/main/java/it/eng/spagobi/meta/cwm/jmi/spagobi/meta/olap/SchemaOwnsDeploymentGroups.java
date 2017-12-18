package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SchemaOwnsDeploymentGroups
  extends RefAssociation
{
  public abstract boolean exists(CwmDeploymentGroup paramCwmDeploymentGroup, CwmSchema paramCwmSchema);
  
  public abstract Collection getDeploymentGroup(CwmSchema paramCwmSchema);
  
  public abstract CwmSchema getSchema(CwmDeploymentGroup paramCwmDeploymentGroup);
  
  public abstract boolean add(CwmDeploymentGroup paramCwmDeploymentGroup, CwmSchema paramCwmSchema);
  
  public abstract boolean remove(CwmDeploymentGroup paramCwmDeploymentGroup, CwmSchema paramCwmSchema);
}
