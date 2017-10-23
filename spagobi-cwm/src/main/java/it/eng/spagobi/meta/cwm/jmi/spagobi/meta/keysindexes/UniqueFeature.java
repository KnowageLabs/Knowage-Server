package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;
import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface UniqueFeature
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmUniqueKey paramCwmUniqueKey);
  
  public abstract List getFeature(CwmUniqueKey paramCwmUniqueKey);
  
  public abstract Collection getUniqueKey(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmUniqueKey paramCwmUniqueKey);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmUniqueKey paramCwmUniqueKey);
}
