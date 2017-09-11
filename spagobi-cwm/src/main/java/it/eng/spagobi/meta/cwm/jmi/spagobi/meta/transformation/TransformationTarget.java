package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TransformationTarget
  extends RefAssociation
{
  public abstract boolean exists(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getTargetTransformation(CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getTarget(CwmTransformation paramCwmTransformation);
  
  public abstract boolean add(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract boolean remove(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
}
