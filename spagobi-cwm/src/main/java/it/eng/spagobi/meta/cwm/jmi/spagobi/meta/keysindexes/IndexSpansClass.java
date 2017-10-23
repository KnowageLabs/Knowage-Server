package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface IndexSpansClass
  extends RefAssociation
{
  public abstract boolean exists(CwmClass paramCwmClass, CwmIndex paramCwmIndex);
  
  public abstract CwmClass getSpannedClass(CwmIndex paramCwmIndex);
  
  public abstract Collection getIndex(CwmClass paramCwmClass);
  
  public abstract boolean add(CwmClass paramCwmClass, CwmIndex paramCwmIndex);
  
  public abstract boolean remove(CwmClass paramCwmClass, CwmIndex paramCwmIndex);
}
