package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClassifier;
import java.util.Collection;

public abstract interface CwmComponent
  extends CwmClassifier
{
  public abstract Collection getDesignPackage();
}
