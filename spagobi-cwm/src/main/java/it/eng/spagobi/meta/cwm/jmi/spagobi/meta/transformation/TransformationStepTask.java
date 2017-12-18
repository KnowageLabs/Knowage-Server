package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TransformationStepTask
  extends RefAssociation
{
  public abstract boolean exists(CwmTransformationStep paramCwmTransformationStep, CwmTransformationTask paramCwmTransformationTask);
  
  public abstract Collection getStep(CwmTransformationTask paramCwmTransformationTask);
  
  public abstract CwmTransformationTask getTask(CwmTransformationStep paramCwmTransformationStep);
  
  public abstract boolean add(CwmTransformationStep paramCwmTransformationStep, CwmTransformationTask paramCwmTransformationTask);
  
  public abstract boolean remove(CwmTransformationStep paramCwmTransformationStep, CwmTransformationTask paramCwmTransformationTask);
}
