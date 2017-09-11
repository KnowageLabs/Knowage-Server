package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.CwmQueryExpression;
import javax.jmi.reflect.RefClass;

public abstract interface CwmViewClass
  extends RefClass
{
  public abstract CwmView createCwmView();
  
  public abstract CwmView createCwmView(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, CwmQueryExpression paramCwmQueryExpression);
}
