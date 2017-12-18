package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmFeature;
import java.util.List;

public abstract interface CwmBehavioralFeature
  extends CwmFeature
{
  public abstract boolean isQuery();
  
  public abstract void setQuery(boolean paramBoolean);
  
  public abstract List getParameter();
}
