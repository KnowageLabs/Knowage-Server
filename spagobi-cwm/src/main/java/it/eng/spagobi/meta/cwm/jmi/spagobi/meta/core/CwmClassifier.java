package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.List;

public abstract interface CwmClassifier
  extends CwmNamespace
{
  public abstract boolean isAbstract();
  
  public abstract void setAbstract(boolean paramBoolean);
  
  public abstract List getFeature();
}
