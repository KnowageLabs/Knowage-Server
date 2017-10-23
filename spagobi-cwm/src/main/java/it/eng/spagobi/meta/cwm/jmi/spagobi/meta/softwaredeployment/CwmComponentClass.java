package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmComponentClass
  extends RefClass
{
  public abstract CwmComponent createCwmComponent();
  
  public abstract CwmComponent createCwmComponent(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
