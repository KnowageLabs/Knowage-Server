package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.List;

public abstract interface CwmKeyRelationship
  extends CwmModelElement
{
  public abstract List getFeature();
  
  public abstract CwmUniqueKey getUniqueKey();
  
  public abstract void setUniqueKey(CwmUniqueKey paramCwmUniqueKey);
}
