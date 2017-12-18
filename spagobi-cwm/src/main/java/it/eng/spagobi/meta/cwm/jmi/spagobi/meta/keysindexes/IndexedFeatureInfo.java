package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import java.util.List;
import javax.jmi.reflect.RefAssociation;

public abstract interface IndexedFeatureInfo
  extends RefAssociation
{
  public abstract boolean exists(CwmIndex paramCwmIndex, CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract CwmIndex getIndex(CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract List getIndexedFeature(CwmIndex paramCwmIndex);
  
  public abstract boolean add(CwmIndex paramCwmIndex, CwmIndexedFeature paramCwmIndexedFeature);
  
  public abstract boolean remove(CwmIndex paramCwmIndex, CwmIndexedFeature paramCwmIndexedFeature);
}
