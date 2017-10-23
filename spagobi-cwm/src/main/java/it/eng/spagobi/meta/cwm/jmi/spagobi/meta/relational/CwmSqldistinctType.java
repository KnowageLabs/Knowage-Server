package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.CwmTypeAlias;

public abstract interface CwmSqldistinctType
  extends CwmSqldataType, CwmTypeAlias
{
  public abstract Integer getLength();
  
  public abstract void setLength(Integer paramInteger);
  
  public abstract Integer getPrecision();
  
  public abstract void setPrecision(Integer paramInteger);
  
  public abstract Integer getScale();
  
  public abstract void setScale(Integer paramInteger);
  
  public abstract CwmSqlsimpleType getSqlSimpleType();
  
  public abstract void setSqlSimpleType(CwmSqlsimpleType paramCwmSqlsimpleType);
}
