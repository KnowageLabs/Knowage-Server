package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment.CwmComponent;
import java.util.Collection;

public abstract interface CwmTransformationTask
  extends CwmComponent
{
  public abstract Collection getTransformation();
  
  public abstract Collection getInverseTask();
  
  public abstract Collection getOriginalTask();
}
