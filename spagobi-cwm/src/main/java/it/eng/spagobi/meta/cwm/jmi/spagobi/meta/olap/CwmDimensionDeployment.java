package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmDimensionDeployment
  extends CwmClass
{
  public abstract CwmHierarchyLevelAssociation getHierarchyLevelAssociation();
  
  public abstract void setHierarchyLevelAssociation(CwmHierarchyLevelAssociation paramCwmHierarchyLevelAssociation);
  
  public abstract CwmValueBasedHierarchy getValueBasedHierarchy();
  
  public abstract void setValueBasedHierarchy(CwmValueBasedHierarchy paramCwmValueBasedHierarchy);
  
  public abstract Collection getStructureMap();
  
  public abstract CwmStructureMap getListOfValues();
  
  public abstract void setListOfValues(CwmStructureMap paramCwmStructureMap);
  
  public abstract CwmStructureMap getImmediateParent();
  
  public abstract void setImmediateParent(CwmStructureMap paramCwmStructureMap);
  
  public abstract CwmDeploymentGroup getDeploymentGroup();
  
  public abstract void setDeploymentGroup(CwmDeploymentGroup paramCwmDeploymentGroup);
}
