package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DistinctTypeHasSimpleType
  extends RefAssociation
{
  public abstract boolean exists(CwmSqldistinctType paramCwmSqldistinctType, CwmSqlsimpleType paramCwmSqlsimpleType);
  
  public abstract Collection getSqlDistinctType(CwmSqlsimpleType paramCwmSqlsimpleType);
  
  public abstract CwmSqlsimpleType getSqlSimpleType(CwmSqldistinctType paramCwmSqldistinctType);
  
  public abstract boolean add(CwmSqldistinctType paramCwmSqldistinctType, CwmSqlsimpleType paramCwmSqlsimpleType);
  
  public abstract boolean remove(CwmSqldistinctType paramCwmSqldistinctType, CwmSqlsimpleType paramCwmSqlsimpleType);
}
