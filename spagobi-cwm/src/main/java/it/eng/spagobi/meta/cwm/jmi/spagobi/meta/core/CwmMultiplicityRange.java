package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmMultiplicityRange
  extends CwmElement
{
  public abstract int getLower();
  
  public abstract void setLower(int paramInt);
  
  public abstract int getUpper();
  
  public abstract void setUpper(int paramInt);
  
  public abstract CwmMultiplicity getMultiplicity();
  
  public abstract void setMultiplicity(CwmMultiplicity paramCwmMultiplicity);
}
