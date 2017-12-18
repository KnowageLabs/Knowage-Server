/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.ModelPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.olap.OlapModelFactory
 * @model kind="package"
 * @generated
 */
public interface OlapModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "olap";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///it/eng/spagobi/meta/model/olapl.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "it.eng.spagobi.meta.model.olap";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	OlapModelPackage eINSTANCE = it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl <em>Olap Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getOlapModel()
	 * @generated
	 */
	int OLAP_MODEL = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Parent Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__PARENT_MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Cubes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__CUBES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Virtual Cubes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__VIRTUAL_CUBES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Dimensions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL__DIMENSIONS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Olap Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OLAP_MODEL_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;


	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl <em>Cube</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.CubeImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getCube()
	 * @generated
	 */
	int CUBE = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Dimensions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__DIMENSIONS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Measures</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__MEASURES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Calculated Members</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__CALCULATED_MEMBERS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Named Sets</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE__NAMED_SETS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Cube</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUBE_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.DimensionImpl <em>Dimension</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.DimensionImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getDimension()
	 * @generated
	 */
	int DIMENSION = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Hierarchies</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__HIERARCHIES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Dimension</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIMENSION_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.HierarchyImpl <em>Hierarchy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.HierarchyImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getHierarchy()
	 * @generated
	 */
	int HIERARCHY = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Dimension</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__DIMENSION = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Levels</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY__LEVELS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Hierarchy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIERARCHY_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl <em>Level</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.LevelImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getLevel()
	 * @generated
	 */
	int LEVEL = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Hierarchy</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__HIERARCHY = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__COLUMN = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Ordinal Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__ORDINAL_COLUMN = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Name Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__NAME_COLUMN = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Caption Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__CAPTION_COLUMN = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Property Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__PROPERTY_COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Level</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.MeasureImpl <em>Measure</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.MeasureImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getMeasure()
	 * @generated
	 */
	int MEASURE = 5;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Cube</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE__COLUMN = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Measure</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURE_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl <em>Calculated Member</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getCalculatedMember()
	 * @generated
	 */
	int CALCULATED_MEMBER = 6;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Hierarchy</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER__HIERARCHY = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Calculated Member</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_MEMBER_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.NamedSetImpl <em>Named Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.NamedSetImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getNamedSet()
	 * @generated
	 */
	int NAMED_SET = 7;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Cube</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET__CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Named Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_SET_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeImpl <em>Virtual Cube</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCube()
	 * @generated
	 */
	int VIRTUAL_CUBE = 8;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Cubes</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__CUBES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Dimensions</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__DIMENSIONS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Measures</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__MEASURES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Calculated Members</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__CALCULATED_MEMBERS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Virtual Cube</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl <em>Virtual Cube Dimension</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCubeDimension()
	 * @generated
	 */
	int VIRTUAL_CUBE_DIMENSION = 9;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Virtual Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Dimension</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION__DIMENSION = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Virtual Cube Dimension</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_DIMENSION_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeMeasureImpl <em>Virtual Cube Measure</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeMeasureImpl
	 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCubeMeasure()
	 * @generated
	 */
	int VIRTUAL_CUBE_MEASURE = 10;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Virtual Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__VIRTUAL_CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__CUBE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE__MEASURE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Virtual Cube Measure</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VIRTUAL_CUBE_MEASURE_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;


	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.OlapModel <em>Olap Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Olap Model</em>'.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel
	 * @generated
	 */
	EClass getOlapModel();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.OlapModel#getParentModel <em>Parent Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent Model</em>'.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getParentModel()
	 * @see #getOlapModel()
	 * @generated
	 */
	EReference getOlapModel_ParentModel();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.OlapModel#getCubes <em>Cubes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Cubes</em>'.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getCubes()
	 * @see #getOlapModel()
	 * @generated
	 */
	EReference getOlapModel_Cubes();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.OlapModel#getVirtualCubes <em>Virtual Cubes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Virtual Cubes</em>'.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getVirtualCubes()
	 * @see #getOlapModel()
	 * @generated
	 */
	EReference getOlapModel_VirtualCubes();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.OlapModel#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Dimensions</em>'.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getDimensions()
	 * @see #getOlapModel()
	 * @generated
	 */
	EReference getOlapModel_Dimensions();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.Cube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube
	 * @generated
	 */
	EClass getCube();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.Cube#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getModel()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_Model();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Cube#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Table</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getTable()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_Table();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.olap.Cube#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Dimensions</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getDimensions()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_Dimensions();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.Cube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Measures</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getMeasures()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_Measures();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Cube#getCalculatedMembers <em>Calculated Members</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Calculated Members</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getCalculatedMembers()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_CalculatedMembers();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.Cube#getNamedSets <em>Named Sets</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Named Sets</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Cube#getNamedSets()
	 * @see #getCube()
	 * @generated
	 */
	EReference getCube_NamedSets();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.Dimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Dimension</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Dimension
	 * @generated
	 */
	EClass getDimension();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Dimension#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Table</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Dimension#getTable()
	 * @see #getDimension()
	 * @generated
	 */
	EReference getDimension_Table();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.Dimension#getHierarchies <em>Hierarchies</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Hierarchies</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Dimension#getHierarchies()
	 * @see #getDimension()
	 * @generated
	 */
	EReference getDimension_Hierarchies();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.Dimension#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Dimension#getModel()
	 * @see #getDimension()
	 * @generated
	 */
	EReference getDimension_Model();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.Hierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Hierarchy</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy
	 * @generated
	 */
	EClass getHierarchy();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Table</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy#getTable()
	 * @see #getHierarchy()
	 * @generated
	 */
	EReference getHierarchy_Table();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Dimension</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy#getDimension()
	 * @see #getHierarchy()
	 * @generated
	 */
	EReference getHierarchy_Dimension();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getLevels <em>Levels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Levels</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy#getLevels()
	 * @see #getHierarchy()
	 * @generated
	 */
	EReference getHierarchy_Levels();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.Level <em>Level</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Level</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level
	 * @generated
	 */
	EClass getLevel();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.Level#getHierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Hierarchy</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getHierarchy()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_Hierarchy();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Level#getColumn <em>Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Column</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getColumn()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_Column();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Level#getOrdinalColumn <em>Ordinal Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ordinal Column</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getOrdinalColumn()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_OrdinalColumn();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Level#getNameColumn <em>Name Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Name Column</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getNameColumn()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_NameColumn();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Level#getCaptionColumn <em>Caption Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Caption Column</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getCaptionColumn()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_CaptionColumn();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.olap.Level#getPropertyColumns <em>Property Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Property Columns</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Level#getPropertyColumns()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_PropertyColumns();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.Measure <em>Measure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Measure</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Measure
	 * @generated
	 */
	EClass getMeasure();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.Measure#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Measure#getCube()
	 * @see #getMeasure()
	 * @generated
	 */
	EReference getMeasure_Cube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.Measure#getColumn <em>Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Column</em>'.
	 * @see it.eng.spagobi.meta.model.olap.Measure#getColumn()
	 * @see #getMeasure()
	 * @generated
	 */
	EReference getMeasure_Column();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.CalculatedMember <em>Calculated Member</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Calculated Member</em>'.
	 * @see it.eng.spagobi.meta.model.olap.CalculatedMember
	 * @generated
	 */
	EClass getCalculatedMember();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.CalculatedMember#getCube()
	 * @see #getCalculatedMember()
	 * @generated
	 */
	EReference getCalculatedMember_Cube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getHierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Hierarchy</em>'.
	 * @see it.eng.spagobi.meta.model.olap.CalculatedMember#getHierarchy()
	 * @see #getCalculatedMember()
	 * @generated
	 */
	EReference getCalculatedMember_Hierarchy();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.NamedSet <em>Named Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Named Set</em>'.
	 * @see it.eng.spagobi.meta.model.olap.NamedSet
	 * @generated
	 */
	EClass getNamedSet();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.NamedSet#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.NamedSet#getCube()
	 * @see #getNamedSet()
	 * @generated
	 */
	EReference getNamedSet_Cube();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.VirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Virtual Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube
	 * @generated
	 */
	EClass getVirtualCube();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getCubes <em>Cubes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Cubes</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getCubes()
	 * @see #getVirtualCube()
	 * @generated
	 */
	EReference getVirtualCube_Cubes();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Dimensions</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions()
	 * @see #getVirtualCube()
	 * @generated
	 */
	EReference getVirtualCube_Dimensions();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Measures</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getMeasures()
	 * @see #getVirtualCube()
	 * @generated
	 */
	EReference getVirtualCube_Measures();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getCalculatedMembers <em>Calculated Members</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Calculated Members</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getCalculatedMembers()
	 * @see #getVirtualCube()
	 * @generated
	 */
	EReference getVirtualCube_CalculatedMembers();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getModel()
	 * @see #getVirtualCube()
	 * @generated
	 */
	EReference getVirtualCube_Model();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension <em>Virtual Cube Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Virtual Cube Dimension</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension
	 * @generated
	 */
	EClass getVirtualCubeDimension();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Virtual Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube()
	 * @see #getVirtualCubeDimension()
	 * @generated
	 */
	EReference getVirtualCubeDimension_VirtualCube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getCube()
	 * @see #getVirtualCubeDimension()
	 * @generated
	 */
	EReference getVirtualCubeDimension_Cube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getDimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Dimension</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getDimension()
	 * @see #getVirtualCubeDimension()
	 * @generated
	 */
	EReference getVirtualCubeDimension_Dimension();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure <em>Virtual Cube Measure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Virtual Cube Measure</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure
	 * @generated
	 */
	EClass getVirtualCubeMeasure();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Virtual Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube()
	 * @see #getVirtualCubeMeasure()
	 * @generated
	 */
	EReference getVirtualCubeMeasure_VirtualCube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Cube</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getCube()
	 * @see #getVirtualCubeMeasure()
	 * @generated
	 */
	EReference getVirtualCubeMeasure_Cube();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getMeasure <em>Measure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Measure</em>'.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getMeasure()
	 * @see #getVirtualCubeMeasure()
	 * @generated
	 */
	EReference getVirtualCubeMeasure_Measure();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	OlapModelFactory getOlapModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl <em>Olap Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getOlapModel()
		 * @generated
		 */
		EClass OLAP_MODEL = eINSTANCE.getOlapModel();
		/**
		 * The meta object literal for the '<em><b>Parent Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OLAP_MODEL__PARENT_MODEL = eINSTANCE.getOlapModel_ParentModel();
		/**
		 * The meta object literal for the '<em><b>Cubes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OLAP_MODEL__CUBES = eINSTANCE.getOlapModel_Cubes();
		/**
		 * The meta object literal for the '<em><b>Virtual Cubes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OLAP_MODEL__VIRTUAL_CUBES = eINSTANCE.getOlapModel_VirtualCubes();
		/**
		 * The meta object literal for the '<em><b>Dimensions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OLAP_MODEL__DIMENSIONS = eINSTANCE.getOlapModel_Dimensions();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl <em>Cube</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.CubeImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getCube()
		 * @generated
		 */
		EClass CUBE = eINSTANCE.getCube();
		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__MODEL = eINSTANCE.getCube_Model();
		/**
		 * The meta object literal for the '<em><b>Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__TABLE = eINSTANCE.getCube_Table();
		/**
		 * The meta object literal for the '<em><b>Dimensions</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__DIMENSIONS = eINSTANCE.getCube_Dimensions();
		/**
		 * The meta object literal for the '<em><b>Measures</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__MEASURES = eINSTANCE.getCube_Measures();
		/**
		 * The meta object literal for the '<em><b>Calculated Members</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__CALCULATED_MEMBERS = eINSTANCE.getCube_CalculatedMembers();
		/**
		 * The meta object literal for the '<em><b>Named Sets</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CUBE__NAMED_SETS = eINSTANCE.getCube_NamedSets();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.DimensionImpl <em>Dimension</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.DimensionImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getDimension()
		 * @generated
		 */
		EClass DIMENSION = eINSTANCE.getDimension();
		/**
		 * The meta object literal for the '<em><b>Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DIMENSION__TABLE = eINSTANCE.getDimension_Table();
		/**
		 * The meta object literal for the '<em><b>Hierarchies</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DIMENSION__HIERARCHIES = eINSTANCE.getDimension_Hierarchies();
		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DIMENSION__MODEL = eINSTANCE.getDimension_Model();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.HierarchyImpl <em>Hierarchy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.HierarchyImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getHierarchy()
		 * @generated
		 */
		EClass HIERARCHY = eINSTANCE.getHierarchy();
		/**
		 * The meta object literal for the '<em><b>Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIERARCHY__TABLE = eINSTANCE.getHierarchy_Table();
		/**
		 * The meta object literal for the '<em><b>Dimension</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIERARCHY__DIMENSION = eINSTANCE.getHierarchy_Dimension();
		/**
		 * The meta object literal for the '<em><b>Levels</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIERARCHY__LEVELS = eINSTANCE.getHierarchy_Levels();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl <em>Level</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.LevelImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getLevel()
		 * @generated
		 */
		EClass LEVEL = eINSTANCE.getLevel();
		/**
		 * The meta object literal for the '<em><b>Hierarchy</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__HIERARCHY = eINSTANCE.getLevel_Hierarchy();
		/**
		 * The meta object literal for the '<em><b>Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__COLUMN = eINSTANCE.getLevel_Column();
		/**
		 * The meta object literal for the '<em><b>Ordinal Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__ORDINAL_COLUMN = eINSTANCE.getLevel_OrdinalColumn();
		/**
		 * The meta object literal for the '<em><b>Name Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__NAME_COLUMN = eINSTANCE.getLevel_NameColumn();
		/**
		 * The meta object literal for the '<em><b>Caption Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__CAPTION_COLUMN = eINSTANCE.getLevel_CaptionColumn();
		/**
		 * The meta object literal for the '<em><b>Property Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__PROPERTY_COLUMNS = eINSTANCE.getLevel_PropertyColumns();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.MeasureImpl <em>Measure</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.MeasureImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getMeasure()
		 * @generated
		 */
		EClass MEASURE = eINSTANCE.getMeasure();
		/**
		 * The meta object literal for the '<em><b>Cube</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MEASURE__CUBE = eINSTANCE.getMeasure_Cube();
		/**
		 * The meta object literal for the '<em><b>Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MEASURE__COLUMN = eINSTANCE.getMeasure_Column();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl <em>Calculated Member</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getCalculatedMember()
		 * @generated
		 */
		EClass CALCULATED_MEMBER = eINSTANCE.getCalculatedMember();
		/**
		 * The meta object literal for the '<em><b>Cube</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CALCULATED_MEMBER__CUBE = eINSTANCE.getCalculatedMember_Cube();
		/**
		 * The meta object literal for the '<em><b>Hierarchy</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CALCULATED_MEMBER__HIERARCHY = eINSTANCE.getCalculatedMember_Hierarchy();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.NamedSetImpl <em>Named Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.NamedSetImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getNamedSet()
		 * @generated
		 */
		EClass NAMED_SET = eINSTANCE.getNamedSet();
		/**
		 * The meta object literal for the '<em><b>Cube</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NAMED_SET__CUBE = eINSTANCE.getNamedSet_Cube();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeImpl <em>Virtual Cube</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCube()
		 * @generated
		 */
		EClass VIRTUAL_CUBE = eINSTANCE.getVirtualCube();
		/**
		 * The meta object literal for the '<em><b>Cubes</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE__CUBES = eINSTANCE.getVirtualCube_Cubes();
		/**
		 * The meta object literal for the '<em><b>Dimensions</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE__DIMENSIONS = eINSTANCE.getVirtualCube_Dimensions();
		/**
		 * The meta object literal for the '<em><b>Measures</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE__MEASURES = eINSTANCE.getVirtualCube_Measures();
		/**
		 * The meta object literal for the '<em><b>Calculated Members</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE__CALCULATED_MEMBERS = eINSTANCE.getVirtualCube_CalculatedMembers();
		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE__MODEL = eINSTANCE.getVirtualCube_Model();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl <em>Virtual Cube Dimension</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCubeDimension()
		 * @generated
		 */
		EClass VIRTUAL_CUBE_DIMENSION = eINSTANCE.getVirtualCubeDimension();
		/**
		 * The meta object literal for the '<em><b>Virtual Cube</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE = eINSTANCE.getVirtualCubeDimension_VirtualCube();
		/**
		 * The meta object literal for the '<em><b>Cube</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_DIMENSION__CUBE = eINSTANCE.getVirtualCubeDimension_Cube();
		/**
		 * The meta object literal for the '<em><b>Dimension</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_DIMENSION__DIMENSION = eINSTANCE.getVirtualCubeDimension_Dimension();
		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeMeasureImpl <em>Virtual Cube Measure</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.olap.impl.VirtualCubeMeasureImpl
		 * @see it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl#getVirtualCubeMeasure()
		 * @generated
		 */
		EClass VIRTUAL_CUBE_MEASURE = eINSTANCE.getVirtualCubeMeasure();
		/**
		 * The meta object literal for the '<em><b>Virtual Cube</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_MEASURE__VIRTUAL_CUBE = eINSTANCE.getVirtualCubeMeasure_VirtualCube();
		/**
		 * The meta object literal for the '<em><b>Cube</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_MEASURE__CUBE = eINSTANCE.getVirtualCubeMeasure_Cube();
		/**
		 * The meta object literal for the '<em><b>Measure</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VIRTUAL_CUBE_MEASURE__MEASURE = eINSTANCE.getVirtualCubeMeasure_Measure();

	}

} //OlapModelPackage
