package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmAttribute;
import java.util.Collection;

public abstract interface CwmDimensionedObject
  extends CwmAttribute
{
  public abstract Collection getDimension();
  
  public abstract CwmSchema getSchema();
  
  public abstract void setSchema(CwmSchema paramCwmSchema);
}
