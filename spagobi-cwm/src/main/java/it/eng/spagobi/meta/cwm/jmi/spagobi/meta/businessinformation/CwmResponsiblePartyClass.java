package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmResponsiblePartyClass
  extends RefClass
{
  public abstract CwmResponsibleParty createCwmResponsibleParty();
  
  public abstract CwmResponsibleParty createCwmResponsibleParty(String paramString1, VisibilityKind paramVisibilityKind, String paramString2);
}
