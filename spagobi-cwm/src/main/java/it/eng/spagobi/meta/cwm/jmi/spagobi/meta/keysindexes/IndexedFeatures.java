package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface IndexedFeatures
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract CwmStructuralFeature getFeature(CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract Collection getIndexedFeature(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmIndexedFeature paramCwmIndexedFeature);
}
