package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ClassifierMapToCfmap
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifierFeatureMap paramCwmClassifierFeatureMap, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract Collection getCfMap(CwmClassifierMap paramCwmClassifierMap);
  
  public abstract CwmClassifierMap getClassifierMap(CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract boolean add(CwmClassifierFeatureMap paramCwmClassifierFeatureMap, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract boolean remove(CwmClassifierFeatureMap paramCwmClassifierFeatureMap, CwmClassifierMap paramCwmClassifierMap);
}
