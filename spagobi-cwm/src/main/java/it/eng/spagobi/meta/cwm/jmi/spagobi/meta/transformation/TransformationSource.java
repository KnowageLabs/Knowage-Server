package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TransformationSource
  extends RefAssociation
{
  public abstract boolean exists(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getSourceTransformation(CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract Collection getSource(CwmTransformation paramCwmTransformation);
  
  public abstract boolean add(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
  
  public abstract boolean remove(CwmTransformation paramCwmTransformation, CwmDataObjectSet paramCwmDataObjectSet);
}
