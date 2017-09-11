package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.Collection;

public abstract interface CwmOperation
  extends CwmBehavioralFeature
{
  public abstract boolean isAbstract();
  
  public abstract void setAbstract(boolean paramBoolean);
  
  public abstract Collection getMethod();
}
