package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmResourceLocatorClass
  extends RefClass
{
  public abstract CwmResourceLocator createCwmResourceLocator();
  
  public abstract CwmResourceLocator createCwmResourceLocator(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
