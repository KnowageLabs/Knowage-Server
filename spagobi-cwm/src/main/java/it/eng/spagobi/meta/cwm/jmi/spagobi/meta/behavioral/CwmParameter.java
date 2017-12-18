package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmExpression;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;

public abstract interface CwmParameter
  extends CwmModelElement
{
  public abstract CwmExpression getDefaultValue();
  
  public abstract void setDefaultValue(CwmExpression paramCwmExpression);
  
  public abstract ParameterDirectionKind getKind();
  
  public abstract void setKind(ParameterDirectionKind paramParameterDirectionKind);
  
  public abstract CwmBehavioralFeature getBehavioralFeature();
  
  public abstract void setBehavioralFeature(CwmBehavioralFeature paramCwmBehavioralFeature);
  
  public abstract CwmEvent getEvent();
  
  public abstract void setEvent(CwmEvent paramCwmEvent);
  
  public abstract CwmClassifier getType();
  
  public abstract void setType(CwmClassifier paramCwmClassifier);
}
