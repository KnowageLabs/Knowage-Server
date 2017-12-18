package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmFeature;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface NodeFeature
  extends RefAssociation
{
  public abstract boolean exists(CwmFeature paramCwmFeature, CwmFeatureNode paramCwmFeatureNode);
  
  public abstract CwmFeature getFeature(CwmFeatureNode paramCwmFeatureNode);
  
  public abstract Collection getFeatureNode(CwmFeature paramCwmFeature);
  
  public abstract boolean add(CwmFeature paramCwmFeature, CwmFeatureNode paramCwmFeatureNode);
  
  public abstract boolean remove(CwmFeature paramCwmFeature, CwmFeatureNode paramCwmFeatureNode);
}
