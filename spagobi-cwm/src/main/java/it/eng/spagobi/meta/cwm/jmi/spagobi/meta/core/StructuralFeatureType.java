package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface StructuralFeatureType
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmClassifier paramCwmClassifier);
  
  public abstract Collection getStructuralFeature(CwmClassifier paramCwmClassifier);
  
  public abstract CwmClassifier getType(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmClassifier paramCwmClassifier);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmClassifier paramCwmClassifier);
}
