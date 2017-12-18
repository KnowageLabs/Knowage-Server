package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import java.util.Collection;

public abstract interface CwmDocument
  extends CwmNamespace
{
  public abstract String getReference();
  
  public abstract void setReference(String paramString);
  
  public abstract String getType();
  
  public abstract void setType(String paramString);
  
  public abstract Collection getModelElement();
}
