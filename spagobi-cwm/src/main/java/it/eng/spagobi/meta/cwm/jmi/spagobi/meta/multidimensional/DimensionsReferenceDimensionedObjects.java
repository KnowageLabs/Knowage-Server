package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionsReferenceDimensionedObjects
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension, CwmDimensionedObject paramCwmDimensionedObject);
  
  public abstract Collection getDimension(CwmDimensionedObject paramCwmDimensionedObject);
  
  public abstract List getDimensionedObject(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension, CwmDimensionedObject paramCwmDimensionedObject);
  
  public abstract boolean remove(CwmDimension paramCwmDimension, CwmDimensionedObject paramCwmDimensionedObject);
}
