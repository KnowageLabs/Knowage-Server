package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmMemberSelection
  extends CwmClass
{
  public abstract CwmDimension getDimension();
  
  public abstract void setDimension(CwmDimension paramCwmDimension);
  
  public abstract Collection getMemberSelectionGroup();
}
