package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDescriptionClass
  extends RefClass
{
  public abstract CwmDescription createCwmDescription();
  
  public abstract CwmDescription createCwmDescription(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3, String paramString4);
}
