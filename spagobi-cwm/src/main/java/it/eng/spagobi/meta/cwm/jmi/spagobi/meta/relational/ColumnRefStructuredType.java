package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ColumnRefStructuredType
  extends RefAssociation
{
  public abstract boolean exists(CwmSqlstructuredType paramCwmSqlstructuredType, CwmColumn paramCwmColumn);
  
  public abstract CwmSqlstructuredType getReferencedTableType(CwmColumn paramCwmColumn);
  
  public abstract Collection getReferencingColumn(CwmSqlstructuredType paramCwmSqlstructuredType);
  
  public abstract boolean add(CwmSqlstructuredType paramCwmSqlstructuredType, CwmColumn paramCwmColumn);
  
  public abstract boolean remove(CwmSqlstructuredType paramCwmSqlstructuredType, CwmColumn paramCwmColumn);
}
