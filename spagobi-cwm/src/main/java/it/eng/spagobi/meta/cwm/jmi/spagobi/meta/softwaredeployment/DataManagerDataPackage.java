package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface DataManagerDataPackage
  extends RefAssociation
{
  public abstract boolean exists(CwmPackage paramCwmPackage, CwmDataManager paramCwmDataManager);
  
  public abstract Collection getDataPackage(CwmDataManager paramCwmDataManager);
  
  public abstract Collection getDataManager(CwmPackage paramCwmPackage);
  
  public abstract boolean add(CwmPackage paramCwmPackage, CwmDataManager paramCwmDataManager);
  
  public abstract boolean remove(CwmPackage paramCwmPackage, CwmDataManager paramCwmDataManager);
}
