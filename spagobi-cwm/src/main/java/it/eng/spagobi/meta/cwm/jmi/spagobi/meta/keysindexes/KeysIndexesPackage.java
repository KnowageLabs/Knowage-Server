package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes;

import javax.jmi.reflect.RefPackage;

public abstract interface KeysIndexesPackage
  extends RefPackage
{
  public abstract CwmUniqueKeyClass getCwmUniqueKey();
  
  public abstract CwmIndexClass getCwmIndex();
  
  public abstract CwmKeyRelationshipClass getCwmKeyRelationship();
  
  public abstract CwmIndexedFeatureClass getCwmIndexedFeature();
  
  public abstract IndexedFeatures getIndexedFeatures();
  
  public abstract IndexedFeatureInfo getIndexedFeatureInfo();
  
  public abstract KeyRelationshipFeatures getKeyRelationshipFeatures();
  
  public abstract UniqueFeature getUniqueFeature();
  
  public abstract UniqueKeyRelationship getUniqueKeyRelationship();
  
  public abstract IndexSpansClass getIndexSpansClass();
}
