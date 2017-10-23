package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSiteClass
  extends RefClass
{
  public abstract CwmSite createCwmSite();
  
  public abstract CwmSite createCwmSite(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);
}
