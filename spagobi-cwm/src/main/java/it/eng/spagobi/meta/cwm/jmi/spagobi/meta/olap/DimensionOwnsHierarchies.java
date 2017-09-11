package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionOwnsHierarchies
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension, CwmHierarchy paramCwmHierarchy);
  
  public abstract CwmDimension getDimension(CwmHierarchy paramCwmHierarchy);
  
  public abstract Collection getHierarchy(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension, CwmHierarchy paramCwmHierarchy);
  
  public abstract boolean remove(CwmDimension paramCwmDimension, CwmHierarchy paramCwmHierarchy);
}
