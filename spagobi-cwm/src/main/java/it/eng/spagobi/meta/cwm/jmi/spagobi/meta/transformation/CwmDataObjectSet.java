package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmModelElement;
import java.util.Collection;

public abstract interface CwmDataObjectSet
  extends CwmModelElement
{
  public abstract Collection getElement();
  
  public abstract Collection getSourceTransformation();
  
  public abstract Collection getTargetTransformation();
}
