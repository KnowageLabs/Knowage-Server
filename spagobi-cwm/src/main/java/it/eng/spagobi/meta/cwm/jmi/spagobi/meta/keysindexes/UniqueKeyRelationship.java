package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface UniqueKeyRelationship
  extends RefAssociation
{
  public abstract boolean exists(CwmKeyRelationship paramCwmKeyRelationship, CwmUniqueKey paramCwmUniqueKey);
  
  public abstract Collection getKeyRelationship(CwmUniqueKey paramCwmUniqueKey);
  
  public abstract CwmUniqueKey getUniqueKey(CwmKeyRelationship paramCwmKeyRelationship);
  
  public abstract boolean add(CwmKeyRelationship paramCwmKeyRelationship, CwmUniqueKey paramCwmUniqueKey);
  
  public abstract boolean remove(CwmKeyRelationship paramCwmKeyRelationship, CwmUniqueKey paramCwmUniqueKey);
}
