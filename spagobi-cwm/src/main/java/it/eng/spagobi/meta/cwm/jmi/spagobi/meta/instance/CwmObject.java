package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import java.util.Collection;

public abstract interface CwmObject
  extends CwmInstance
{
  public abstract Collection getSlot();
}
