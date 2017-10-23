package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CfmapFeature
  extends RefAssociation
{
  public abstract boolean exists(CwmFeature paramCwmFeature, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract Collection getFeature(CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract Collection getCfMap(CwmFeature paramCwmFeature);
  
  public abstract boolean add(CwmFeature paramCwmFeature, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract boolean remove(CwmFeature paramCwmFeature, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
}
