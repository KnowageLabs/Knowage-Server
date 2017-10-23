package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional;

import javax.jmi.reflect.RefPackage;

public abstract interface MultidimensionalPackage
  extends RefPackage
{
  public abstract CwmDimensionClass getCwmDimension();
  
  public abstract CwmDimensionedObjectClass getCwmDimensionedObject();
  
  public abstract CwmMemberClass getCwmMember();
  
  public abstract CwmMemberSetClass getCwmMemberSet();
  
  public abstract CwmMemberValueClass getCwmMemberValue();
  
  public abstract CwmSchemaClass getCwmSchema();
  
  public abstract DimensionsReferenceDimensionedObjects getDimensionsReferenceDimensionedObjects();
  
  public abstract CompositesReferenceComponents getCompositesReferenceComponents();
  
  public abstract MdschemaOwnsDimensionedObjects getMdschemaOwnsDimensionedObjects();
  
  public abstract MdschemaOwnsDimensions getMdschemaOwnsDimensions();
  
  public abstract DimensionOwnsMemberSets getDimensionOwnsMemberSets();
}
