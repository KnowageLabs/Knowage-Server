package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionOwnsMemberSets
  extends RefAssociation
{
  public abstract boolean exists(CwmMemberSet paramCwmMemberSet, CwmDimension paramCwmDimension);
  
  public abstract Collection getMemberSet(CwmDimension paramCwmDimension);
  
  public abstract CwmDimension getDimension(CwmMemberSet paramCwmMemberSet);
  
  public abstract boolean add(CwmMemberSet paramCwmMemberSet, CwmDimension paramCwmDimension);
  
  public abstract boolean remove(CwmMemberSet paramCwmMemberSet, CwmDimension paramCwmDimension);
}
