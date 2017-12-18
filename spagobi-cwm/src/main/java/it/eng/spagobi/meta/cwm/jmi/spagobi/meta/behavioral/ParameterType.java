package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ParameterType
  extends RefAssociation
{
  public abstract boolean exists(CwmParameter paramCwmParameter, CwmClassifier paramCwmClassifier);
  
  public abstract Collection getParameter(CwmClassifier paramCwmClassifier);
  
  public abstract CwmClassifier getType(CwmParameter paramCwmParameter);
  
  public abstract boolean add(CwmParameter paramCwmParameter, CwmClassifier paramCwmClassifier);
  
  public abstract boolean remove(CwmParameter paramCwmParameter, CwmClassifier paramCwmClassifier);
}
