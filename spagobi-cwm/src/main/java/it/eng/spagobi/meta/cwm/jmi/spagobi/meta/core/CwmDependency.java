package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmDependency
  extends CwmModelElement
{
  public abstract String getKind();
  
  public abstract void setKind(String paramString);
  
  public abstract Collection getClient();
  
  public abstract Collection getSupplier();
}
