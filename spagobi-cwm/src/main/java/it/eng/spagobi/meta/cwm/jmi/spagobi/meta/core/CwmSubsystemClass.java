package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefClass;

public abstract interface CwmSubsystemClass
  extends RefClass
{
  public abstract CwmSubsystem createCwmSubsystem();
  
  public abstract CwmSubsystem createCwmSubsystem(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean);
}
