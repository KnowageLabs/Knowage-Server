package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDeploymentGroupClass
  extends RefClass
{
  public abstract CwmDeploymentGroup createCwmDeploymentGroup();
  
  public abstract CwmDeploymentGroup createCwmDeploymentGroup(String paramString, VisibilityKind paramVisibilityKind);
}
