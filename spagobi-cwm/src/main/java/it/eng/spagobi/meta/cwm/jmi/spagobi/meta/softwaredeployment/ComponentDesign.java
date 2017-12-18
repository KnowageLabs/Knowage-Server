package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmPackage;
import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ComponentDesign
  extends RefAssociation
{
  public abstract boolean exists(CwmPackage paramCwmPackage, CwmComponent paramCwmComponent);
  
  public abstract Collection getDesignPackage(CwmComponent paramCwmComponent);
  
  public abstract Collection getComponent(CwmPackage paramCwmPackage);
  
  public abstract boolean add(CwmPackage paramCwmPackage, CwmComponent paramCwmComponent);
  
  public abstract boolean remove(CwmPackage paramCwmPackage, CwmComponent paramCwmComponent);
}
