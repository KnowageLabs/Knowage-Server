package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.util.Collection;

public abstract interface CwmTransformationMap
  extends CwmTransformation
{
  public abstract Collection getClassifierMap();
}
