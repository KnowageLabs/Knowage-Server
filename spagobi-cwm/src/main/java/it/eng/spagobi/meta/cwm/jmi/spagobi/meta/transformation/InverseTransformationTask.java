package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface InverseTransformationTask
  extends RefAssociation
{
  public abstract boolean exists(CwmTransformationTask paramCwmTransformationTask1, CwmTransformationTask paramCwmTransformationTask2);
  
  public abstract Collection getOriginalTask(CwmTransformationTask paramCwmTransformationTask);
  
  public abstract Collection getInverseTask(CwmTransformationTask paramCwmTransformationTask);
  
  public abstract boolean add(CwmTransformationTask paramCwmTransformationTask1, CwmTransformationTask paramCwmTransformationTask2);
  
  public abstract boolean remove(CwmTransformationTask paramCwmTransformationTask1, CwmTransformationTask paramCwmTransformationTask2);
}
