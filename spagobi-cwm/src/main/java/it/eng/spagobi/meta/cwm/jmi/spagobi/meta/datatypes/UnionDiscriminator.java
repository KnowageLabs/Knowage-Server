package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface UnionDiscriminator
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmUnion paramCwmUnion);
  
  public abstract CwmStructuralFeature getDiscriminator(CwmUnion paramCwmUnion);
  
  public abstract Collection getDiscriminatedUnion(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmUnion paramCwmUnion);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmUnion paramCwmUnion);
}
