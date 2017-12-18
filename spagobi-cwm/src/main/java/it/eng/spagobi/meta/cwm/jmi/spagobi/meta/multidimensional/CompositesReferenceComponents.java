package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CompositesReferenceComponents
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension1, CwmDimension paramCwmDimension2);
  
  public abstract Collection getComposite(CwmDimension paramCwmDimension);
  
  public abstract Collection getComponent(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension1, CwmDimension paramCwmDimension2);
  
  public abstract boolean remove(CwmDimension paramCwmDimension1, CwmDimension paramCwmDimension2);
}
