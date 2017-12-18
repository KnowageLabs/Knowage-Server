package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import javax.jmi.reflect.RefPackage;

public abstract interface TransformationPackage
  extends RefPackage
{
  public abstract CwmTransformationClass getCwmTransformation();
  
  public abstract CwmDataObjectSetClass getCwmDataObjectSet();
  
  public abstract CwmTransformationTaskClass getCwmTransformationTask();
  
  public abstract CwmTransformationStepClass getCwmTransformationStep();
  
  public abstract CwmTransformationActivityClass getCwmTransformationActivity();
  
  public abstract CwmPrecedenceConstraintClass getCwmPrecedenceConstraint();
  
  public abstract CwmTransformationUseClass getCwmTransformationUse();
  
  public abstract CwmTransformationMapClass getCwmTransformationMap();
  
  public abstract CwmTransformationTreeClass getCwmTransformationTree();
  
  public abstract CwmClassifierMapClass getCwmClassifierMap();
  
  public abstract CwmFeatureMapClass getCwmFeatureMap();
  
  public abstract CwmStepPrecedenceClass getCwmStepPrecedence();
  
  public abstract CwmClassifierFeatureMapClass getCwmClassifierFeatureMap();
  
  public abstract TransformationSource getTransformationSource();
  
  public abstract TransformationTarget getTransformationTarget();
  
  public abstract TransformationStepTask getTransformationStepTask();
  
  public abstract InverseTransformationTask getInverseTransformationTask();
  
  public abstract DataObjectSetElement getDataObjectSetElement();
  
  public abstract TransformationTaskElement getTransformationTaskElement();
  
  public abstract ClassifierMapToFeatureMap getClassifierMapToFeatureMap();
  
  public abstract ClassifierMapToCfmap getClassifierMapToCfmap();
  
  public abstract ClassifierMapSource getClassifierMapSource();
  
  public abstract ClassifierMapTarget getClassifierMapTarget();
  
  public abstract FeatureMapTarget getFeatureMapTarget();
  
  public abstract FeatureMapSource getFeatureMapSource();
  
  public abstract CfmapClassifier getCfmapClassifier();
  
  public abstract CfmapFeature getCfmapFeature();
}
