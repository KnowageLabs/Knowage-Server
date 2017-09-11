package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmKeyRelationshipClass
  extends RefClass
{
  public abstract CwmKeyRelationship createCwmKeyRelationship();
  
  public abstract CwmKeyRelationship createCwmKeyRelationship(String paramString, VisibilityKind paramVisibilityKind);
}
