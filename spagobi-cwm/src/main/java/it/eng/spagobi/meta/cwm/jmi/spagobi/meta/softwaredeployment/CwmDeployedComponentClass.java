package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDeployedComponentClass
  extends RefClass
{
  public abstract CwmDeployedComponent createCwmDeployedComponent();
  
  public abstract CwmDeployedComponent createCwmDeployedComponent(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
