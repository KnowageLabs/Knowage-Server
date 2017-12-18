package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TransformationTaskElement
  extends RefAssociation
{
  public abstract boolean exists(CwmTransformationTask paramCwmTransformationTask, CwmTransformation paramCwmTransformation);
  
  public abstract Collection getTask(CwmTransformation paramCwmTransformation);
  
  public abstract Collection getTransformation(CwmTransformationTask paramCwmTransformationTask);
  
  public abstract boolean add(CwmTransformationTask paramCwmTransformationTask, CwmTransformation paramCwmTransformation);
  
  public abstract boolean remove(CwmTransformationTask paramCwmTransformationTask, CwmTransformation paramCwmTransformation);
}
