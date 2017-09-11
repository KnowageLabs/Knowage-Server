package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCatalogClass
  extends RefClass
{
  public abstract CwmCatalog createCwmCatalog();
  
  public abstract CwmCatalog createCwmCatalog(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
