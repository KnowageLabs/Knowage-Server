package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMethodClass
  extends RefClass
{
  public abstract CwmMethod createCwmMethod();
  
  public abstract CwmMethod createCwmMethod(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind, boolean paramBoolean, CwmProcedureExpression paramCwmProcedureExpression);
}
