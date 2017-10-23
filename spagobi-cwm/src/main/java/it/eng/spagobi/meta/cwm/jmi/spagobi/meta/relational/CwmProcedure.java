package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral.CwmMethod;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.ProcedureType;

public abstract interface CwmProcedure
  extends CwmMethod
{
  public abstract ProcedureType getType();
  
  public abstract void setType(ProcedureType paramProcedureType);
}
