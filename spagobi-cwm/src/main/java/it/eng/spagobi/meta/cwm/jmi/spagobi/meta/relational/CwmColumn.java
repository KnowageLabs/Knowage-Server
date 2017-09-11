package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmAttribute;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.NullableType;

public abstract interface CwmColumn
  extends CwmAttribute
{
  public abstract Integer getPrecision();
  
  public abstract void setPrecision(Integer paramInteger);
  
  public abstract Integer getScale();
  
  public abstract void setScale(Integer paramInteger);
  
  public abstract NullableType getIsNullable();
  
  public abstract void setIsNullable(NullableType paramNullableType);
  
  public abstract Integer getLength();
  
  public abstract void setLength(Integer paramInteger);
  
  public abstract String getCollationName();
  
  public abstract void setCollationName(String paramString);
  
  public abstract String getCharacterSetName();
  
  public abstract void setCharacterSetName(String paramString);
  
  public abstract CwmNamedColumnSet getOptionScopeColumnSet();
  
  public abstract void setOptionScopeColumnSet(CwmNamedColumnSet paramCwmNamedColumnSet);
  
  public abstract CwmSqlstructuredType getReferencedTableType();
  
  public abstract void setReferencedTableType(CwmSqlstructuredType paramCwmSqlstructuredType);
}
