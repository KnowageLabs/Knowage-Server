package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeOwnsCubeDimensionAssociations
  extends RefAssociation
{
  public abstract boolean exists(CwmCube paramCwmCube, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract CwmCube getCube(CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract Collection getCubeDimensionAssociation(CwmCube paramCwmCube);
  
  public abstract boolean add(CwmCube paramCwmCube, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract boolean remove(CwmCube paramCwmCube, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
}
