package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance.CwmExtent;

public abstract interface CwmMemberSet
  extends CwmExtent
{
  public abstract CwmDimension getDimension();
  
  public abstract void setDimension(CwmDimension paramCwmDimension);
}
