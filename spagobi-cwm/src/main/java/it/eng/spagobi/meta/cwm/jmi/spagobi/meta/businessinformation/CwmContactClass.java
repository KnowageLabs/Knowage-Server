package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmContactClass
  extends RefClass
{
  public abstract CwmContact createCwmContact();
  
  public abstract CwmContact createCwmContact(String paramString, VisibilityKind paramVisibilityKind);
}
