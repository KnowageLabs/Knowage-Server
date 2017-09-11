package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmMultiplicityClass
  extends RefClass
{
  public abstract CwmMultiplicity createCwmMultiplicity();
}
