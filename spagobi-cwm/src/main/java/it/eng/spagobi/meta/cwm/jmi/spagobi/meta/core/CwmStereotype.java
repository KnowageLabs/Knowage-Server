package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmStereotype
  extends CwmModelElement
{
  public abstract String getBaseClass();
  
  public abstract void setBaseClass(String paramString);
  
  public abstract Collection getExtendedElement();
  
  public abstract Collection getRequiredTag();
  
  public abstract Collection getStereotypeConstraint();
}
