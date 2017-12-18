package it.eng.spagobi.meta.cwm.jmi.spagobi;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.MetaPackage;
import javax.jmi.reflect.RefPackage;

public abstract interface SpagobiPackage
  extends RefPackage
{
  public abstract MetaPackage getMeta();
}
