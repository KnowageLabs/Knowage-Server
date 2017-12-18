package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import javax.jmi.reflect.RefPackage;

public abstract interface InstancePackage
  extends RefPackage
{
  public abstract CwmDataSlotClass getCwmDataSlot();
  
  public abstract CwmDataValueClass getCwmDataValue();
  
  public abstract CwmExtentClass getCwmExtent();
  
  public abstract CwmInstanceClass getCwmInstance();
  
  public abstract CwmObjectClass getCwmObject();
  
  public abstract CwmSlotClass getCwmSlot();
  
  public abstract SlotValue getSlotValue();
  
  public abstract InstanceClassifier getInstanceClassifier();
  
  public abstract ObjectSlot getObjectSlot();
  
  public abstract FeatureSlot getFeatureSlot();
  
  public abstract DataSlotType getDataSlotType();
}
