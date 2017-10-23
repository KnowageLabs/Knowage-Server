package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmAttribute;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;

public abstract interface CwmUnionMember
  extends CwmAttribute
{
  public abstract CwmExpression getMemberCase();
  
  public abstract void setMemberCase(CwmExpression paramCwmExpression);
  
  public abstract boolean isDefault();
  
  public abstract void setDefault(boolean paramBoolean);
}
