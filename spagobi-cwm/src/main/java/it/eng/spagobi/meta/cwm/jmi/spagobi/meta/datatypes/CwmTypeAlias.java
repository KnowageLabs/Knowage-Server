package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDataType;

public abstract interface CwmTypeAlias
  extends CwmDataType
{
  public abstract CwmClassifier getType();
  
  public abstract void setType(CwmClassifier paramCwmClassifier);
}
