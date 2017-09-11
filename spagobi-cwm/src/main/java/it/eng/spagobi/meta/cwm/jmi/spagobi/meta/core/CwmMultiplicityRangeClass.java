package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmMultiplicityRangeClass
  extends RefClass
{
  public abstract CwmMultiplicityRange createCwmMultiplicityRange();
  
  public abstract CwmMultiplicityRange createCwmMultiplicityRange(int paramInt1, int paramInt2);
}
