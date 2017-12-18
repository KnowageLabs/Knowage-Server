package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.List;

public abstract interface CwmHierarchyLevelAssociation
  extends CwmClass
{
  public abstract CwmLevelBasedHierarchy getLevelBasedHierarchy();
  
  public abstract void setLevelBasedHierarchy(CwmLevelBasedHierarchy paramCwmLevelBasedHierarchy);
  
  public abstract CwmLevel getCurrentLevel();
  
  public abstract void setCurrentLevel(CwmLevel paramCwmLevel);
  
  public abstract List getDimensionDeployment();
}
