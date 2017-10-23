package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface ClassifierFeature
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmFeature paramCwmFeature);
  
  public abstract CwmClassifier getOwner(CwmFeature paramCwmFeature);
  
  public abstract List getFeature(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmFeature paramCwmFeature);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmFeature paramCwmFeature);
}
