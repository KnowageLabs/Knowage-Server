package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface TriggerUsingColumnSet
  extends RefAssociation
{
  public abstract boolean exists(CwmNamedColumnSet paramCwmNamedColumnSet, CwmTrigger paramCwmTrigger);
  
  public abstract Collection getUsedColumnSet(CwmTrigger paramCwmTrigger);
  
  public abstract Collection getUsingTrigger(CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract boolean add(CwmNamedColumnSet paramCwmNamedColumnSet, CwmTrigger paramCwmTrigger);
  
  public abstract boolean remove(CwmNamedColumnSet paramCwmNamedColumnSet, CwmTrigger paramCwmTrigger);
}
