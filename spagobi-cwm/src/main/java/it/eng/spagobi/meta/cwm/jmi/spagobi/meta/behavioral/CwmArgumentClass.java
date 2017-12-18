package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmArgumentClass
  extends RefClass
{
  public abstract CwmArgument createCwmArgument();
  
  public abstract CwmArgument createCwmArgument(String paramString, VisibilityKind paramVisibilityKind, CwmExpression paramCwmExpression);
}
