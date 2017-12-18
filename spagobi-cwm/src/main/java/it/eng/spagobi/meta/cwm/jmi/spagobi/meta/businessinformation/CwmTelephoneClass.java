package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTelephoneClass
  extends RefClass
{
  public abstract CwmTelephone createCwmTelephone();
  
  public abstract CwmTelephone createCwmTelephone(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
