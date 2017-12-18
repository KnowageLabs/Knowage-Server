package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCubeDimensionAssociationClass
  extends RefClass
{
  public abstract CwmCubeDimensionAssociation createCwmCubeDimensionAssociation();
  
  public abstract CwmCubeDimensionAssociation createCwmCubeDimensionAssociation(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
