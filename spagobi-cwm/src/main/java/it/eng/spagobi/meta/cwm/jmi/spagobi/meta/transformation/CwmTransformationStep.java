package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmNamespace;
import java.util.Collection;

public abstract interface CwmTransformationStep
  extends CwmModelElement
{
  public abstract CwmTransformationTask getTask();
  
  public abstract void setTask(CwmTransformationTask paramCwmTransformationTask);
  
  public abstract CwmNamespace getActivity();
  
  public abstract void setActivity(CwmNamespace paramCwmNamespace);
  
  public abstract Collection getPrecedence();
  
  public abstract Collection getPrecedingStep();
  
  public abstract Collection getSucceedingStep();
}
