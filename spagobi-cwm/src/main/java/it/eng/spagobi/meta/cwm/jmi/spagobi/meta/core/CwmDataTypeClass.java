package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmDataTypeClass
  extends RefClass
{
  public abstract CwmDataType createCwmDataType();
  
  public abstract CwmDataType createCwmDataType(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
