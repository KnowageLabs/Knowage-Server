package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmSubsystem;
import java.util.Collection;

public abstract interface CwmTransformationActivity
  extends CwmSubsystem
{
  public abstract String getCreationDate();
  
  public abstract void setCreationDate(String paramString);
  
  public abstract Collection getStep();
}
