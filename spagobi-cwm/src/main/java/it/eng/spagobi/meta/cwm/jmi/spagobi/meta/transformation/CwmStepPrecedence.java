package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDependency;
import java.util.Collection;

public abstract interface CwmStepPrecedence
  extends CwmDependency
{
  public abstract Collection getPrecedingStep();
  
  public abstract Collection getSucceedingStep();
}
