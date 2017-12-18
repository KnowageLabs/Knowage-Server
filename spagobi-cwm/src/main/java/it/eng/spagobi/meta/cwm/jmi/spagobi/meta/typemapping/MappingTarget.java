package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface MappingTarget
  extends RefAssociation
{
  public abstract boolean exists(CwmClassifier paramCwmClassifier, CwmTypeMapping paramCwmTypeMapping);
  
  public abstract CwmClassifier getTargetType(CwmTypeMapping paramCwmTypeMapping);
  
  public abstract Collection getMappingTo(CwmClassifier paramCwmClassifier);
  
  public abstract boolean add(CwmClassifier paramCwmClassifier, CwmTypeMapping paramCwmTypeMapping);
  
  public abstract boolean remove(CwmClassifier paramCwmClassifier, CwmTypeMapping paramCwmTypeMapping);
}
