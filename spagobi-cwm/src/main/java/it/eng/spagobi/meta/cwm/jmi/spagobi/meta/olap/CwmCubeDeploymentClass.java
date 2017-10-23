package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCubeDeploymentClass
  extends RefClass
{
  public abstract CwmCubeDeployment createCwmCubeDeployment();
  
  public abstract CwmCubeDeployment createCwmCubeDeployment(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
