package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ColumnSetOfStructuredType
  extends RefAssociation
{
  public abstract boolean exists(CwmSqlstructuredType paramCwmSqlstructuredType, CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract CwmSqlstructuredType getType(CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract Collection getColumnSet(CwmSqlstructuredType paramCwmSqlstructuredType);
  
  public abstract boolean add(CwmSqlstructuredType paramCwmSqlstructuredType, CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract boolean remove(CwmSqlstructuredType paramCwmSqlstructuredType, CwmNamedColumnSet paramCwmNamedColumnSet);
}
