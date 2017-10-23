package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmGeneralization
  extends CwmModelElement
{
  public abstract CwmClassifier getChild();
  
  public abstract void setChild(CwmClassifier paramCwmClassifier);
  
  public abstract CwmClassifier getParent();
  
  public abstract void setParent(CwmClassifier paramCwmClassifier);
}
