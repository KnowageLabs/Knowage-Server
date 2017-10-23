package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap;

import javax.jmi.reflect.RefPackage;

public abstract interface OlapPackage
  extends RefPackage
{
  public abstract CwmContentMapClass getCwmContentMap();
  
  public abstract CwmCubeClass getCwmCube();
  
  public abstract CwmCubeDeploymentClass getCwmCubeDeployment();
  
  public abstract CwmCubeDimensionAssociationClass getCwmCubeDimensionAssociation();
  
  public abstract CwmCubeRegionClass getCwmCubeRegion();
  
  public abstract CwmDeploymentGroupClass getCwmDeploymentGroup();
  
  public abstract CwmDimensionClass getCwmDimension();
  
  public abstract CwmDimensionDeploymentClass getCwmDimensionDeployment();
  
  public abstract CwmHierarchyClass getCwmHierarchy();
  
  public abstract CwmHierarchyLevelAssociationClass getCwmHierarchyLevelAssociation();
  
  public abstract CwmLevelBasedHierarchyClass getCwmLevelBasedHierarchy();
  
  public abstract CwmMemberSelectionGroupClass getCwmMemberSelectionGroup();
  
  public abstract CwmMemberSelectionClass getCwmMemberSelection();
  
  public abstract CwmSchemaClass getCwmSchema();
  
  public abstract CwmValueBasedHierarchyClass getCwmValueBasedHierarchy();
  
  public abstract CwmLevelClass getCwmLevel();
  
  public abstract CwmCodedLevelClass getCwmCodedLevel();
  
  public abstract CwmMeasureClass getCwmMeasure();
  
  public abstract CwmStructureMapClass getCwmStructureMap();
  
  public abstract CwmHierarchyMemberSelectionGroupClass getCwmHierarchyMemberSelectionGroup();
  
  public abstract LevelBasedHierarchyOwnsHierarchyLevelAssociations getLevelBasedHierarchyOwnsHierarchyLevelAssociations();
  
  public abstract HierarchyLevelAssocsReferenceLevel getHierarchyLevelAssocsReferenceLevel();
  
  public abstract DimensionOwnsMemberSelections getDimensionOwnsMemberSelections();
  
  public abstract CubeOwnsCubeDimensionAssociations getCubeOwnsCubeDimensionAssociations();
  
  public abstract CubeDimensionAssociationsReferenceDimension getCubeDimensionAssociationsReferenceDimension();
  
  public abstract DimensionOwnsHierarchies getDimensionOwnsHierarchies();
  
  public abstract DimensionHasDefaultHierarchy getDimensionHasDefaultHierarchy();
  
  public abstract CubeDimensionAssociationsReferenceCalcHierarchy getCubeDimensionAssociationsReferenceCalcHierarchy();
  
  public abstract MemberSelectionGroupReferencesMemberSelections getMemberSelectionGroupReferencesMemberSelections();
  
  public abstract SchemaOwnsCubes getSchemaOwnsCubes();
  
  public abstract SchemaOwnsDimensions getSchemaOwnsDimensions();
  
  public abstract HierarchyLevelAssociationOwnsDimensionDeployments getHierarchyLevelAssociationOwnsDimensionDeployments();
  
  public abstract ValueBasedHierarchyOwnsDimensionDeployments getValueBasedHierarchyOwnsDimensionDeployments();
  
  public abstract DimensionDeploymentOwnsStructureMaps getDimensionDeploymentOwnsStructureMaps();
  
  public abstract DimensionDeploymentHasListOfValues getDimensionDeploymentHasListOfValues();
  
  public abstract DimensionDeploymentHasImmediateParent getDimensionDeploymentHasImmediateParent();
  
  public abstract CubeOwnsCubeRegions getCubeOwnsCubeRegions();
  
  public abstract CubeRegionOwnsMemberSelectionGroups getCubeRegionOwnsMemberSelectionGroups();
  
  public abstract CubeRegionOwnsCubeDeployments getCubeRegionOwnsCubeDeployments();
  
  public abstract SchemaOwnsDeploymentGroups getSchemaOwnsDeploymentGroups();
  
  public abstract DeploymentGroupReferencesCubeDeployments getDeploymentGroupReferencesCubeDeployments();
  
  public abstract DeploymentGroupReferencesDimensionDeployments getDeploymentGroupReferencesDimensionDeployments();
  
  public abstract CubeDeploymentOwnsContentMaps getCubeDeploymentOwnsContentMaps();
  
  public abstract HierarchyMemberSelectionGroupReferencesHierarchy getHierarchyMemberSelectionGroupReferencesHierarchy();
}
