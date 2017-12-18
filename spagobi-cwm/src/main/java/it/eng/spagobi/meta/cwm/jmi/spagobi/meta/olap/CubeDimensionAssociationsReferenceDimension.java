package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface CubeDimensionAssociationsReferenceDimension
  extends RefAssociation
{
  public abstract boolean exists(CwmDimension paramCwmDimension, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract CwmDimension getDimension(CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract Collection getCubeDimensionAssociation(CwmDimension paramCwmDimension);
  
  public abstract boolean add(CwmDimension paramCwmDimension, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
  
  public abstract boolean remove(CwmDimension paramCwmDimension, CwmCubeDimensionAssociation paramCwmCubeDimensionAssociation);
}
