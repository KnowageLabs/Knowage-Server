package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmBooleanExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmProcedureExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ActionOrientationType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ConditionTimingType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.EventManipulationType;
import java.util.Collection;

public abstract interface CwmTrigger
  extends CwmModelElement
{
  public abstract EventManipulationType getEventManipulation();
  
  public abstract void setEventManipulation(EventManipulationType paramEventManipulationType);
  
  public abstract CwmBooleanExpression getActionCondition();
  
  public abstract void setActionCondition(CwmBooleanExpression paramCwmBooleanExpression);
  
  public abstract CwmProcedureExpression getActionStatement();
  
  public abstract void setActionStatement(CwmProcedureExpression paramCwmProcedureExpression);
  
  public abstract ActionOrientationType getActionOrientation();
  
  public abstract void setActionOrientation(ActionOrientationType paramActionOrientationType);
  
  public abstract ConditionTimingType getConditionTiming();
  
  public abstract void setConditionTiming(ConditionTimingType paramConditionTimingType);
  
  public abstract String getConditionReferenceNewTable();
  
  public abstract void setConditionReferenceNewTable(String paramString);
  
  public abstract String getConditionReferenceOldTable();
  
  public abstract void setConditionReferenceOldTable(String paramString);
  
  public abstract CwmTable getTable();
  
  public abstract void setTable(CwmTable paramCwmTable);
  
  public abstract Collection getUsedColumnSet();
}
