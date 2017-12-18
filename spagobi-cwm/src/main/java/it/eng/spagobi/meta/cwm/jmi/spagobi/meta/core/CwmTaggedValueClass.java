package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmTaggedValueClass
  extends RefClass
{
  public abstract CwmTaggedValue createCwmTaggedValue();
  
  public abstract CwmTaggedValue createCwmTaggedValue(String paramString1, String paramString2);
}
