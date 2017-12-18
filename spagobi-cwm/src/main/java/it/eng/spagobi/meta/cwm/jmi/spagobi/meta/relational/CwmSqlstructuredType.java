package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CwmClass;
import java.util.Collection;

public abstract interface CwmSqlstructuredType
  extends CwmSqldataType, CwmClass
{
  public abstract Collection getReferencingColumn();
  
  public abstract Collection getColumnSet();
}
