package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;

public abstract interface CwmIndexedFeature
  extends CwmModelElement
{
  public abstract Boolean isAscending();
  
  public abstract void setAscending(Boolean paramBoolean);
  
  public abstract CwmStructuralFeature getFeature();
  
  public abstract void setFeature(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract CwmIndex getIndex();
  
  public abstract void setIndex(CwmIndex paramCwmIndex);
}
