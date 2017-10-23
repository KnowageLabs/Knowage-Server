package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMemberSelectionGroupClass
  extends RefClass
{
  public abstract CwmMemberSelectionGroup createCwmMemberSelectionGroup();
  
  public abstract CwmMemberSelectionGroup createCwmMemberSelectionGroup(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
