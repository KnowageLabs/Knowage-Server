package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DimensionOwnsMemberSelections
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension, CwmMemberSelection paramCwmMemberSelection);
  
  public abstract CwmDimension getDimension(CwmMemberSelection paramCwmMemberSelection);
  
  public abstract Collection getMemberSelection(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension, CwmMemberSelection paramCwmMemberSelection);
  
  public abstract boolean remove(CwmDimension paramCwmDimension, CwmMemberSelection paramCwmMemberSelection);
}
