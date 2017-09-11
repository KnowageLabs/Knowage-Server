package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import javax.jmi.reflect.RefPackage;

public abstract interface CorePackage
  extends RefPackage
{
  public abstract CwmElementClass getCwmElement();
  
  public abstract CwmModelElementClass getCwmModelElement();
  
  public abstract CwmNamespaceClass getCwmNamespace();
  
  public abstract CwmClassifierClass getCwmClassifier();
  
  public abstract CwmClassClass getCwmClass();
  
  public abstract CwmDataTypeClass getCwmDataType();
  
  public abstract CwmPackageClass getCwmPackage();
  
  public abstract CwmSubsystemClass getCwmSubsystem();
  
  public abstract CwmModelClass getCwmModel();
  
  public abstract CwmFeatureClass getCwmFeature();
  
  public abstract CwmStructuralFeatureClass getCwmStructuralFeature();
  
  public abstract CwmAttributeClass getCwmAttribute();
  
  public abstract CwmConstraintClass getCwmConstraint();
  
  public abstract CwmDependencyClass getCwmDependency();
  
  public abstract CwmExpressionClass getCwmExpression();
  
  public abstract CwmBooleanExpressionClass getCwmBooleanExpression();
  
  public abstract CwmProcedureExpressionClass getCwmProcedureExpression();
  
  public abstract CwmMultiplicityClass getCwmMultiplicity();
  
  public abstract CwmMultiplicityRangeClass getCwmMultiplicityRange();
  
  public abstract CwmStereotypeClass getCwmStereotype();
  
  public abstract CwmTaggedValueClass getCwmTaggedValue();
  
  public abstract ClassifierFeature getClassifierFeature();
  
  public abstract DependencyClient getDependencyClient();
  
  public abstract DependencySupplier getDependencySupplier();
  
  public abstract ElementConstraint getElementConstraint();
  
  public abstract ElementOwnership getElementOwnership();
  
  public abstract ImportedElements getImportedElements();
  
  public abstract RangeMultiplicity getRangeMultiplicity();
  
  public abstract StereotypeConstraints getStereotypeConstraints();
  
  public abstract StereotypedElement getStereotypedElement();
  
  public abstract StereotypeTaggedValues getStereotypeTaggedValues();
  
  public abstract StructuralFeatureType getStructuralFeatureType();
  
  public abstract TaggedElement getTaggedElement();
}
