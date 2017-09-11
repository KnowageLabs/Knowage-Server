package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmOperationClass
  extends RefClass
{
  public abstract CwmOperation createCwmOperation();
  
  public abstract CwmOperation createCwmOperation(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind, boolean paramBoolean1, boolean paramBoolean2);
}
