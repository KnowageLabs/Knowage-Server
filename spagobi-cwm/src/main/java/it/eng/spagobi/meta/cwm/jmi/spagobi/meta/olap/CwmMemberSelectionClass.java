package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMemberSelectionClass
  extends RefClass
{
  public abstract CwmMemberSelection createCwmMemberSelection();
  
  public abstract CwmMemberSelection createCwmMemberSelection(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
