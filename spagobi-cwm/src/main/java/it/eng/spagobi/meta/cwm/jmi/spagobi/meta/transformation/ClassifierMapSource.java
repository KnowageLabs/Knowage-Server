package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ClassifierMapSource
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract Collection getSource(CwmClassifierMap paramCwmClassifierMap);
  
  public abstract Collection getClassifierMap(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmClassifierMap paramCwmClassifierMap);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmClassifierMap paramCwmClassifierMap);
}
