package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeDimensionAssociationsReferenceCalcHierarchy
  extends RefAssociation
{
  public abstract boolean exists(CwmHierarchy paramCwmHierarchy, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract CwmHierarchy getCalcHierarchy(CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract Collection getCubeDimensionAssociation(CwmHierarchy paramCwmHierarchy);
  
  public abstract boolean add(CwmHierarchy paramCwmHierarchy, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract boolean remove(CwmHierarchy paramCwmHierarchy, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
}
