package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.CwmQueryExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmQueryColumnSetClass
  extends RefClass
{
  public abstract CwmQueryColumnSet createCwmQueryColumnSet();
  
  public abstract CwmQueryColumnSet createCwmQueryColumnSet(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean, CwmQueryExpression paramCwmQueryExpression);
}
