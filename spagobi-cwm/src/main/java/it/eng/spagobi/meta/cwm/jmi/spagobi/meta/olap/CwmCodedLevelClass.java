package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions.CwmExpressionNode;
import javax.jmi.reflect.RefClass;

public abstract interface CwmCodedLevelClass
  extends RefClass
{
  public abstract CwmCodedLevel createCwmCodedLevel();
  
  public abstract CwmCodedLevel createCwmCodedLevel(String paramString, VisibilityKind paramVisibilityKind, boolean paramBoolean, CwmExpressionNode paramCwmExpressionNode);
}
