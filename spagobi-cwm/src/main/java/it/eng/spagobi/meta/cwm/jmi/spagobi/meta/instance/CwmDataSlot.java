package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDataType;

public abstract interface CwmDataSlot
  extends CwmSlot
{
  public abstract String getDataValue();
  
  public abstract void setDataValue(String paramString);
  
  public abstract CwmDataType getDataType();
  
  public abstract void setDataType(CwmDataType paramCwmDataType);
}
