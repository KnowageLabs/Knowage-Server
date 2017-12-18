package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.ScopeKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ProcedureType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmProcedureClass
  extends RefClass
{
  public abstract CwmProcedure createCwmProcedure();
  
  public abstract CwmProcedure createCwmProcedure(String paramString, VisibilityKind paramVisibilityKind, ScopeKind paramScopeKind, boolean paramBoolean, CwmProcedureExpression paramCwmProcedureExpression, ProcedureType paramProcedureType);
}
