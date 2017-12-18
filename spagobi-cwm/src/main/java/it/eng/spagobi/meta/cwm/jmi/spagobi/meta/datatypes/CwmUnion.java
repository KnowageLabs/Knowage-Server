package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;

public abstract interface CwmUnion
  extends CwmClassifier
{
  public abstract CwmStructuralFeature getDiscriminator();
  
  public abstract void setDiscriminator(CwmStructuralFeature paramCwmStructuralFeature);
}
