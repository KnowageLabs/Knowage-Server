package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ParentElement
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmGeneralization paramCwmGeneralization);
  
  public abstract CwmClassifier getParent(CwmGeneralization paramCwmGeneralization);
  
  public abstract Collection getSpecialization(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmGeneralization paramCwmGeneralization);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmGeneralization paramCwmGeneralization);
}
