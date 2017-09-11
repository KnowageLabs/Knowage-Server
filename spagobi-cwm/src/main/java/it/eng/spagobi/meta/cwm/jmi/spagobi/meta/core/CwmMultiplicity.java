package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmMultiplicity
  extends CwmElement
{
  public abstract Collection getRange();
}
