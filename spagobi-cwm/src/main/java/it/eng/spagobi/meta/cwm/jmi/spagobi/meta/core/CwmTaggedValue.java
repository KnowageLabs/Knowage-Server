package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

public abstract interface CwmTaggedValue
  extends CwmElement
{
  public abstract String getTag();
  
  public abstract void setTag(String paramString);
  
  public abstract String getValue();
  
  public abstract void setValue(String paramString);
  
  public abstract CwmModelElement getModelElement();
  
  public abstract void setModelElement(CwmModelElement paramCwmModelElement);
  
  public abstract CwmStereotype getStereotype();
  
  public abstract void setStereotype(CwmStereotype paramCwmStereotype);
}
