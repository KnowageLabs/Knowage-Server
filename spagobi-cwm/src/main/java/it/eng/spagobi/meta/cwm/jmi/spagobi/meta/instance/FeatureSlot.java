package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface FeatureSlot
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmSlot paramCwmSlot);
  
  public abstract CwmStructuralFeature getFeature(CwmSlot paramCwmSlot);
  
  public abstract Collection getSlot(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmSlot paramCwmSlot);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmSlot paramCwmSlot);
}
