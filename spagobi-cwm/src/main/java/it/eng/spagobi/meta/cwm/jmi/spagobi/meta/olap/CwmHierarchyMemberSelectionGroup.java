package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;

public abstract interface CwmHierarchyMemberSelectionGroup
  extends CwmMemberSelectionGroup
{
  public abstract Collection getHierarchy();
}
