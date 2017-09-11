package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmPackageUsageClass
  extends RefClass
{
  public abstract CwmPackageUsage createCwmPackageUsage();
  
  public abstract CwmPackageUsage createCwmPackageUsage(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
