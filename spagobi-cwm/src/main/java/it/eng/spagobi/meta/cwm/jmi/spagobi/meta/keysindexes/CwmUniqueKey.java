package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;
import java.util.List;

public abstract interface CwmUniqueKey
  extends CwmModelElement
{
  public abstract List getFeature();
  
  public abstract Collection getKeyRelationship();
}
