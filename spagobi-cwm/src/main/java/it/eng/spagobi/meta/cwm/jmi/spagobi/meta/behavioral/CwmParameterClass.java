package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmParameterClass
  extends RefClass
{
  public abstract CwmParameter createCwmParameter();
  
  public abstract CwmParameter createCwmParameter(String paramString, VisibilityKind paramVisibilityKind, CwmExpression paramCwmExpression, ParameterDirectionKind paramParameterDirectionKind);
}
