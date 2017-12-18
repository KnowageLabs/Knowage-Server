package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ClassifierAlias
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmTypeAlias paramCwmTypeAlias);
  
  public abstract CwmClassifier getType(CwmTypeAlias paramCwmTypeAlias);
  
  public abstract Collection getAlias(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmTypeAlias paramCwmTypeAlias);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmTypeAlias paramCwmTypeAlias);
}
