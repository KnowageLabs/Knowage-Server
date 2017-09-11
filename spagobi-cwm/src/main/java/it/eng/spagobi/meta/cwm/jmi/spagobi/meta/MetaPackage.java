package it.eng.spagobi.meta.cwm.jmi.spagobi.meta;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral.BehavioralPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.businessinformation.BusinessInformationPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.CorePackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes.DataTypesPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.expressions.ExpressionsPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.instance.InstancePackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.keysindexes.KeysIndexesPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.multidimensional.MultidimensionalPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.olap.OlapPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.RelationalPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships.RelationshipsPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment.SoftwareDeploymentPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation.TransformationPackage;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping.TypeMappingPackage;
import javax.jmi.reflect.RefPackage;

public abstract interface MetaPackage
  extends RefPackage
{
  public abstract CorePackage getCore();
  
  public abstract BehavioralPackage getBehavioral();
  
  public abstract RelationshipsPackage getRelationships();
  
  public abstract InstancePackage getInstance();
  
  public abstract BusinessInformationPackage getBusinessInformation();
  
  public abstract DataTypesPackage getDataTypes();
  
  public abstract ExpressionsPackage getExpressions();
  
  public abstract KeysIndexesPackage getKeysIndexes();
  
  public abstract SoftwareDeploymentPackage getSoftwareDeployment();
  
  public abstract TypeMappingPackage getTypeMapping();
  
  public abstract RelationalPackage getRelational();
  
  public abstract MultidimensionalPackage getMultidimensional();
  
  public abstract TransformationPackage getTransformation();
  
  public abstract OlapPackage getOlap();
}
