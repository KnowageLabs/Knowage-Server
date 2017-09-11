package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDataType;
import java.util.Collection;

public abstract interface CwmEnumeration
  extends CwmDataType
{
  public abstract boolean isOrdered();
  
  public abstract void setOrdered(boolean paramBoolean);
  
  public abstract Collection getLiteral();
}
