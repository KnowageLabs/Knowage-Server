package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmAssociationClass
  extends RefClass
{
  public abstract CwmAssociation createCwmAssociation();
  
  public abstract CwmAssociation createCwmAssociation(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
