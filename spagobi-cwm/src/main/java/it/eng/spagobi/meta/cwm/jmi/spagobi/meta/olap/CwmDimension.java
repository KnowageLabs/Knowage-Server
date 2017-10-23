package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmDimension
  extends CwmClass
{
  public abstract boolean isTime();
  
  public abstract void setTime(boolean paramBoolean);
  
  public abstract boolean isMeasure();
  
  public abstract void setMeasure(boolean paramBoolean);
  
  public abstract Collection getHierarchy();
  
  public abstract Collection getMemberSelection();
  
  public abstract Collection getCubeDimensionAssociation();
  
  public abstract CwmHierarchy getDisplayDefault();
  
  public abstract void setDisplayDefault(CwmHierarchy paramCwmHierarchy);
  
  public abstract CwmSchema getSchema();
  
  public abstract void setSchema(CwmSchema paramCwmSchema);
}
