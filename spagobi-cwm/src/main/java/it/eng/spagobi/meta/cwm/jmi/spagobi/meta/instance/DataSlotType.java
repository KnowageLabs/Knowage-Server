package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmDataType;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DataSlotType
  extends RefAssociation
{
  public abstract boolean exists(CwmDataType paramCwmDataType, CwmDataSlot paramCwmDataSlot);
  
  public abstract CwmDataType getDataType(CwmDataSlot paramCwmDataSlot);
  
  public abstract Collection getDataSlot(CwmDataType paramCwmDataType);
  
  public abstract boolean add(CwmDataType paramCwmDataType, CwmDataSlot paramCwmDataSlot);
  
  public abstract boolean remove(CwmDataType paramCwmDataType, CwmDataSlot paramCwmDataSlot);
}
