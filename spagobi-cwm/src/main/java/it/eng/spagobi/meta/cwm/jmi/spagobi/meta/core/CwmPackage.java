package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;

public abstract interface CwmPackage
  extends CwmNamespace
{
  public abstract Collection getImportedElement();
}
