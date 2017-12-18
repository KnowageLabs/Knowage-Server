package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDataType;

public abstract interface CwmSqlsimpleType
  extends CwmSqldataType, CwmDataType
{
  public abstract Integer getCharacterMaximumLength();
  
  public abstract void setCharacterMaximumLength(Integer paramInteger);
  
  public abstract Integer getCharacterOctetLength();
  
  public abstract void setCharacterOctetLength(Integer paramInteger);
  
  public abstract Integer getNumericPrecision();
  
  public abstract void setNumericPrecision(Integer paramInteger);
  
  public abstract Integer getNumericPrecisionRadix();
  
  public abstract void setNumericPrecisionRadix(Integer paramInteger);
  
  public abstract Integer getNumericScale();
  
  public abstract void setNumericScale(Integer paramInteger);
  
  public abstract Integer getDateTimePrecision();
  
  public abstract void setDateTimePrecision(Integer paramInteger);
}
