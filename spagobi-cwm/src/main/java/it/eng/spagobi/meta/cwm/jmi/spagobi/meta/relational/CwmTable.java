package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.List;

public abstract interface CwmTable
  extends CwmNamedColumnSet
{
  public abstract boolean isTemporary();
  
  public abstract void setTemporary(boolean paramBoolean);
  
  public abstract String getTemporaryScope();
  
  public abstract void setTemporaryScope(String paramString);
  
  public abstract boolean isSystem();
  
  public abstract void setSystem(boolean paramBoolean);
  
  public abstract List getTrigger();
}
