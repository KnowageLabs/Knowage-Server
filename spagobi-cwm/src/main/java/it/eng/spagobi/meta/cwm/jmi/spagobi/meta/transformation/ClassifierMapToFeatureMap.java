package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ClassifierMapToFeatureMap
  extends RefAssociation
{
  public abstract boolean exists(CwmFeatureMap paramCwmFeatureMap, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract Collection getFeatureMap(CwmClassifierMap paramCwmClassifierMap);
  
  public abstract CwmClassifierMap getClassifierMap(CwmFeatureMap paramCwmFeatureMap);
  
  public abstract boolean add(CwmFeatureMap paramCwmFeatureMap, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract boolean remove(CwmFeatureMap paramCwmFeatureMap, CwmClassifierMap paramCwmClassifierMap);
}
