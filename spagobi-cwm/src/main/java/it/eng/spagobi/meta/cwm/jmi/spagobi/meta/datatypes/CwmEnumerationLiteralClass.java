package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmEnumerationLiteralClass
  extends RefClass
{
  public abstract CwmEnumerationLiteral createCwmEnumerationLiteral();
  
  public abstract CwmEnumerationLiteral createCwmEnumerationLiteral(String paramString, VisibilityKind paramVisibilityKind, CwmExpression paramCwmExpression);
}
