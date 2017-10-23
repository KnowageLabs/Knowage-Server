package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping.CwmTypeSystem;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SystemTypespace
  extends RefAssociation
{
  public abstract boolean exists(CwmTypeSystem paramCwmTypeSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract Collection getTypespace(CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract Collection getSupportingSystem(CwmTypeSystem paramCwmTypeSystem);
  
  public abstract boolean add(CwmTypeSystem paramCwmTypeSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
  
  public abstract boolean remove(CwmTypeSystem paramCwmTypeSystem, CwmSoftwareSystem paramCwmSoftwareSystem);
}
