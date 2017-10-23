package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmEmailClass
  extends RefClass
{
  public abstract CwmEmail createCwmEmail();
  
  public abstract CwmEmail createCwmEmail(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
