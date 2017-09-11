package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

public abstract interface CwmDataValue
  extends CwmInstance
{
  public abstract String getValue();
  
  public abstract void setValue(String paramString);
}
