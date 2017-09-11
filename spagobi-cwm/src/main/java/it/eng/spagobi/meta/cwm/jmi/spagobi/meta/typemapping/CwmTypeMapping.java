package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmTypeMapping
  extends CwmModelElement
{
  public abstract boolean isBestMatch();
  
  public abstract void setBestMatch(boolean paramBoolean);
  
  public abstract boolean isLossy();
  
  public abstract void setLossy(boolean paramBoolean);
  
  public abstract CwmClassifier getSourceType();
  
  public abstract void setSourceType(CwmClassifier paramCwmClassifier);
  
  public abstract CwmClassifier getTargetType();
  
  public abstract void setTargetType(CwmClassifier paramCwmClassifier);
}
