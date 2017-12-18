package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface MemberSelectionGroupReferencesMemberSelections
  extends RefAssociation
{
  public abstract boolean exists(CwmMemberSelection paramCwmMemberSelection, CwmMemberSelectionGroup paramCwmMemberSelectionGroup);
  
  public abstract Collection getMemberSelection(CwmMemberSelectionGroup paramCwmMemberSelectionGroup);
  
  public abstract Collection getMemberSelectionGroup(CwmMemberSelection paramCwmMemberSelection);
  
  public abstract boolean add(CwmMemberSelection paramCwmMemberSelection, CwmMemberSelectionGroup paramCwmMemberSelectionGroup);
  
  public abstract boolean remove(CwmMemberSelection paramCwmMemberSelection, CwmMemberSelectionGroup paramCwmMemberSelectionGroup);
}
