package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmStructuralFeature;
import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface KeyRelationshipFeatures
  extends RefAssociation
{
  public abstract boolean exists(CwmStructuralFeature paramCwmStructuralFeature, CwmKeyRelationship paramCwmKeyRelationship);
  
  public abstract List getFeature(CwmKeyRelationship paramCwmKeyRelationship);
  
  public abstract Collection getKeyRelationship(CwmStructuralFeature paramCwmStructuralFeature);
  
  public abstract boolean add(CwmStructuralFeature paramCwmStructuralFeature, CwmKeyRelationship paramCwmKeyRelationship);
  
  public abstract boolean remove(CwmStructuralFeature paramCwmStructuralFeature, CwmKeyRelationship paramCwmKeyRelationship);
}
