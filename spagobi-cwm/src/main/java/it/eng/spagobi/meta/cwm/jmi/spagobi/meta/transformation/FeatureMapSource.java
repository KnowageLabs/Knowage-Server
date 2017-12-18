package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface FeatureMapSource
  extends RefAssociation
{
  public abstract boolean exists(CwmFeature paramCwmFeature, CwmFeatureMap paramCwmFeatureMap);
  
  public abstract Collection getSource(CwmFeatureMap paramCwmFeatureMap);
  
  public abstract Collection getFeatureMap(CwmFeature paramCwmFeature);
  
  public abstract boolean add(CwmFeature paramCwmFeature, CwmFeatureMap paramCwmFeatureMap);
  
  public abstract boolean remove(CwmFeature paramCwmFeature, CwmFeatureMap paramCwmFeatureMap);
}
