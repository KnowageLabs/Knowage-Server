package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmBooleanExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ActionOrientationType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ConditionTimingType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.EventManipulationType;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTriggerClass
  extends RefClass
{
  public abstract CwmTrigger createCwmTrigger();
  
  public abstract CwmTrigger createCwmTrigger(String paramString1, VisibilityKind paramVisibilityKind, EventManipulationType paramEventManipulationType, CwmBooleanExpression paramCwmBooleanExpression, CwmProcedureExpression paramCwmProcedureExpression, ActionOrientationType paramActionOrientationType, ConditionTimingType paramConditionTimingType, String paramString2, String paramString3);
}
