package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface MdschemaOwnsDimensionedObjects
  extends RefAssociation
{
  public abstract boolean exists(CwmDimensionedObject paramCwmDimensionedObject, CwmSchema paramCwmSchema);
  
  public abstract Collection getDimensionedObject(CwmSchema paramCwmSchema);
  
  public abstract CwmSchema getSchema(CwmDimensionedObject paramCwmDimensionedObject);
  
  public abstract boolean add(CwmDimensionedObject paramCwmDimensionedObject, CwmSchema paramCwmSchema);
  
  public abstract boolean remove(CwmDimensionedObject paramCwmDimensionedObject, CwmSchema paramCwmSchema);
}
