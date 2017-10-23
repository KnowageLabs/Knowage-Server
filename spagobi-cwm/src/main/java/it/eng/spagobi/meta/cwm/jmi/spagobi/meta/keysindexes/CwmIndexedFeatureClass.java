package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import javax.jmi.reflect.RefClass;

public abstract interface CwmIndexedFeatureClass
  extends RefClass
{
  public abstract CwmIndexedFeature createCwmIndexedFeature();
  
  public abstract CwmIndexedFeature createCwmIndexedFeature(String paramString, VisibilityKind paramVisibilityKind, Boolean paramBoolean);
}
