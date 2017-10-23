package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral.ParameterDirectionKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmSqlparameterClass
  extends RefClass
{
  public abstract CwmSqlparameter createCwmSqlparameter();
  
  public abstract CwmSqlparameter createCwmSqlparameter(String paramString, VisibilityKind paramVisibilityKind, CwmExpression paramCwmExpression, ParameterDirectionKind paramParameterDirectionKind);
}
