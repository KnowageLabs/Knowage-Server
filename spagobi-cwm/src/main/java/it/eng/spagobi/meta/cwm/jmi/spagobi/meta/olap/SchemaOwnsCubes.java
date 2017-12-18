package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface SchemaOwnsCubes
  extends RefAssociation
{
  public abstract boolean exists(CwmCube paramCwmCube, CwmSchema paramCwmSchema);
  
  public abstract Collection getCube(CwmSchema paramCwmSchema);
  
  public abstract CwmSchema getSchema(CwmCube paramCwmCube);
  
  public abstract boolean add(CwmCube paramCwmCube, CwmSchema paramCwmSchema);
  
  public abstract boolean remove(CwmCube paramCwmCube, CwmSchema paramCwmSchema);
}
