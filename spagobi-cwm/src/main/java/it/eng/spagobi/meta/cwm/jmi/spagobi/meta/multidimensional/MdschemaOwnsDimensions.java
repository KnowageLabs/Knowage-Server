package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface MdschemaOwnsDimensions
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension, CwmSchema paramCwmSchema);
  
  public abstract Collection getDimension(CwmSchema paramCwmSchema);
  
  public abstract CwmSchema getSchema(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension, CwmSchema paramCwmSchema);
  
  public abstract boolean remove(CwmDimension paramCwmDimension, CwmSchema paramCwmSchema);
}
