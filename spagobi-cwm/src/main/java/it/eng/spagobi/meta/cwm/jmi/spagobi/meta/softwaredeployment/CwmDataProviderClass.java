package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDataProviderClass
  extends RefClass
{
  public abstract CwmDataProvider createCwmDataProvider();
  
  public abstract CwmDataProvider createCwmDataProvider(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, boolean paramBoolean);
}
