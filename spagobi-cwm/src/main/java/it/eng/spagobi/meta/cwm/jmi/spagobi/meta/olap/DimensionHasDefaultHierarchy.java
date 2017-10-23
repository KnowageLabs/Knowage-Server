package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionHasDefaultHierarchy
  extends RefAssociation
{
  public abstract boolean exists(CwmHierarchy paramCwmHierarchy, CwmDimension paramCwmDimension);
  
  public abstract CwmHierarchy getDisplayDefault(CwmDimension paramCwmDimension);
  
  public abstract CwmDimension getDefaultedDimension(CwmHierarchy paramCwmHierarchy);
  
  public abstract boolean add(CwmHierarchy paramCwmHierarchy, CwmDimension paramCwmDimension);
  
  public abstract boolean remove(CwmHierarchy paramCwmHierarchy, CwmDimension paramCwmDimension);
}
