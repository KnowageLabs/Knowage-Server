package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDimensionDeploymentClass
  extends RefClass
{
  public abstract CwmDimensionDeployment createCwmDimensionDeployment();
  
  public abstract CwmDimensionDeployment createCwmDimensionDeployment(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
