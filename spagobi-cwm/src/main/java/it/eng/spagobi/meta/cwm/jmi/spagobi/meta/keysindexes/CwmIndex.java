package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.List;

public abstract interface CwmIndex
  extends CwmModelElement
{
  public abstract boolean isPartitioning();
  
  public abstract void setPartitioning(boolean paramBoolean);
  
  public abstract boolean isSorted();
  
  public abstract void setSorted(boolean paramBoolean);
  
  public abstract boolean isUnique();
  
  public abstract void setUnique(boolean paramBoolean);
  
  public abstract List getIndexedFeature();
  
  public abstract CwmClass getSpannedClass();
  
  public abstract void setSpannedClass(CwmClass paramCwmClass);
}
