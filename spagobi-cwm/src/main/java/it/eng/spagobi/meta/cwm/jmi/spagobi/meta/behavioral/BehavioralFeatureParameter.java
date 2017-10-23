package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface BehavioralFeatureParameter
  extends RefAssociation
{
  public abstract boolean exists(CwmBehavioralFeature paramCwmBehavioralFeature, CwmParameter paramCwmParameter);
  
  public abstract CwmBehavioralFeature getBehavioralFeature(CwmParameter paramCwmParameter);
  
  public abstract List getParameter(CwmBehavioralFeature paramCwmBehavioralFeature);
  
  public abstract boolean add(CwmBehavioralFeature paramCwmBehavioralFeature, CwmParameter paramCwmParameter);
  
  public abstract boolean remove(CwmBehavioralFeature paramCwmBehavioralFeature, CwmParameter paramCwmParameter);
}
