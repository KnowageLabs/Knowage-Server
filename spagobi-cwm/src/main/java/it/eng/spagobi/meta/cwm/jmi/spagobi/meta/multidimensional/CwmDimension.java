package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;
import java.util.List;

public abstract interface CwmDimension
  extends CwmClass
{
  public abstract List getDimensionedObject();
  
  public abstract Collection getComponent();
  
  public abstract Collection getComposite();
  
  public abstract Collection getMemberSet();
  
  public abstract CwmSchema getSchema();
  
  public abstract void setSchema(CwmSchema paramCwmSchema);
}
