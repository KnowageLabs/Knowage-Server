package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface TableOwningTrigger
  extends RefAssociation
{
  public abstract boolean exists(CwmTable paramCwmTable, CwmTrigger paramCwmTrigger);
  
  public abstract CwmTable getTable(CwmTrigger paramCwmTrigger);
  
  public abstract List getTrigger(CwmTable paramCwmTable);
  
  public abstract boolean add(CwmTable paramCwmTable, CwmTrigger paramCwmTrigger);
  
  public abstract boolean remove(CwmTable paramCwmTable, CwmTrigger paramCwmTrigger);
}
