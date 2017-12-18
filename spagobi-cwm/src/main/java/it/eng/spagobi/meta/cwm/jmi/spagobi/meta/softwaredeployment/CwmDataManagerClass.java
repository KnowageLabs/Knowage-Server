package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDataManagerClass
  extends RefClass
{
  public abstract CwmDataManager createCwmDataManager();
  
  public abstract CwmDataManager createCwmDataManager(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, boolean paramBoolean);
}
