package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;

public abstract interface CwmSchema
  extends CwmPackage
{
  public abstract Collection getDimensionedObject();
  
  public abstract Collection getDimension();
}
