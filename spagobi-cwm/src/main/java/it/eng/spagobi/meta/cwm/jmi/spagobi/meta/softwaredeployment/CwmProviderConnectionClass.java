package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmProviderConnectionClass
  extends RefClass
{
  public abstract CwmProviderConnection createCwmProviderConnection();
  
  public abstract CwmProviderConnection createCwmProviderConnection(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
