package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;

public abstract interface CwmSqldataType
  extends CwmClassifier
{
  public abstract Integer getTypeNumber();
  
  public abstract void setTypeNumber(Integer paramInteger);
}
