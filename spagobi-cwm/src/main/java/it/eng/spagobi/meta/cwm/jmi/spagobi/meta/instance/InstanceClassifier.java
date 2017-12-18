package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface InstanceClassifier
  extends RefAssociation
{
  public abstract boolean exists(CwmInstance paramCwmInstance, CwmClassifier paramCwmClassifier);
  
  public abstract Collection getInstance(CwmClassifier paramCwmClassifier);
  
  public abstract CwmClassifier getClassifier(CwmInstance paramCwmInstance);
  
  public abstract boolean add(CwmInstance paramCwmInstance, CwmClassifier paramCwmClassifier);
  
  public abstract boolean remove(CwmInstance paramCwmInstance, CwmClassifier paramCwmClassifier);
}
