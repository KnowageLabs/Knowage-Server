package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmEnumerationLiteral
  extends CwmModelElement
{
  public abstract CwmExpression getValue();
  
  public abstract void setValue(CwmExpression paramCwmExpression);
  
  public abstract CwmEnumeration getEnumeration();
  
  public abstract void setEnumeration(CwmEnumeration paramCwmEnumeration);
}
