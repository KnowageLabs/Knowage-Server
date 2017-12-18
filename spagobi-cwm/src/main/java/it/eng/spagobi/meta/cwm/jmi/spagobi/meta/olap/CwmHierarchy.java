package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmHierarchy
  extends CwmClass
{
  public abstract CwmDimension getDimension();
  
  public abstract void setDimension(CwmDimension paramCwmDimension);
  
  public abstract Collection getCubeDimensionAssociation();
  
  public abstract CwmDimension getDefaultedDimension();
  
  public abstract void setDefaultedDimension(CwmDimension paramCwmDimension);
  
  public abstract Collection getHierarchyMemberSelectionGroup();
}
