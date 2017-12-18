package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;

public abstract interface CwmCubeDimensionAssociation
  extends CwmClass
{
  public abstract CwmDimension getDimension();
  
  public abstract void setDimension(CwmDimension paramCwmDimension);
  
  public abstract CwmCube getCube();
  
  public abstract void setCube(CwmCube paramCwmCube);
  
  public abstract CwmHierarchy getCalcHierarchy();
  
  public abstract void setCalcHierarchy(CwmHierarchy paramCwmHierarchy);
}
