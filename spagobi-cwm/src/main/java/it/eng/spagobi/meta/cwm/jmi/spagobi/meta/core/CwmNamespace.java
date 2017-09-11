package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmNamespace
  extends CwmModelElement
{
  public abstract Collection getOwnedElement();
}
