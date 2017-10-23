package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmExpression
  extends CwmElement
{
  public abstract String getBody();
  
  public abstract void setBody(String paramString);
  
  public abstract String getLanguage();
  
  public abstract void setLanguage(String paramString);
}
