package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;

public abstract interface CwmSlot
  extends CwmModelElement
{
  public abstract CwmObject getObject();
  
  public abstract void setObject(CwmObject paramCwmObject);
  
  public abstract CwmInstance getValue();
  
  public abstract void setValue(CwmInstance paramCwmInstance);
  
  public abstract CwmStructuralFeature getFeature();
  
  public abstract void setFeature(CwmStructuralFeature paramCwmStructuralFeature);
}
