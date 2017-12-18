package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CfmapClassifier
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract Collection getClassifier(CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract Collection getCfMap(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmClassifierFeatureMap paramCwmClassifierFeatureMap);
}
