package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmInstance
  extends CwmModelElement
{
  public abstract CwmClassifier getClassifier();
  
  public abstract void setClassifier(CwmClassifier paramCwmClassifier);
}
