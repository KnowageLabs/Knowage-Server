package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface RangeMultiplicity
  extends RefAssociation
{
  public abstract boolean exists(CwmMultiplicity paramCwmMultiplicity, CwmMultiplicityRange paramCwmMultiplicityRange);
  
  public abstract CwmMultiplicity getMultiplicity(CwmMultiplicityRange paramCwmMultiplicityRange);
  
  public abstract Collection getRange(CwmMultiplicity paramCwmMultiplicity);
  
  public abstract boolean add(CwmMultiplicity paramCwmMultiplicity, CwmMultiplicityRange paramCwmMultiplicityRange);
  
  public abstract boolean remove(CwmMultiplicity paramCwmMultiplicity, CwmMultiplicityRange paramCwmMultiplicityRange);
}
