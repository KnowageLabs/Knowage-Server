package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.List;

public abstract interface CwmLevelBasedHierarchy
  extends CwmHierarchy
{
  public abstract List getHierarchyLevelAssociation();
}
