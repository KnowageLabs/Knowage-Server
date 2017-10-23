package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmTypeAliasClass
  extends RefClass
{
  public abstract CwmTypeAlias createCwmTypeAlias();
  
  public abstract CwmTypeAlias createCwmTypeAlias(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
