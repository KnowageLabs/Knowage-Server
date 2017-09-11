package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ColumnOptionsColumnSet
  extends RefAssociation
{
  public abstract boolean exists(CwmColumn paramCwmColumn, CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract Collection getOptionScopeColumn(CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract CwmNamedColumnSet getOptionScopeColumnSet(CwmColumn paramCwmColumn);
  
  public abstract boolean add(CwmColumn paramCwmColumn, CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract boolean remove(CwmColumn paramCwmColumn, CwmNamedColumnSet paramCwmNamedColumnSet);
}
