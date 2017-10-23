package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDeployedSoftwareSystemClass
  extends RefClass
{
  public abstract CwmDeployedSoftwareSystem createCwmDeployedSoftwareSystem();
  
  public abstract CwmDeployedSoftwareSystem createCwmDeployedSoftwareSystem(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
