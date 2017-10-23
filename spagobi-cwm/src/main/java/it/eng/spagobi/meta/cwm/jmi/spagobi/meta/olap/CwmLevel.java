package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;

public abstract interface CwmLevel
  extends CwmMemberSelection
{
  public abstract Collection getHierarchyLevelAssociation();
}
