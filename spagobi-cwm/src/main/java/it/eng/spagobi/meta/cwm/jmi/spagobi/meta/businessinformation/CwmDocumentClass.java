package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmDocumentClass
  extends RefClass
{
  public abstract CwmDocument createCwmDocument();
  
  public abstract CwmDocument createCwmDocument(String paramString1, VisibilityKind paramVisibilityKind, String paramString2, String paramString3);
}
