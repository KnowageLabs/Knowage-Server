package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmGeneralizationClass
  extends RefClass
{
  public abstract CwmGeneralization createCwmGeneralization();
  
  public abstract CwmGeneralization createCwmGeneralization(String paramString, VisibilityKind paramVisibilityKind);
}
