/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model;

import org.eclipse.emf.ecore.EAttribute;
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
 * @see it.eng.spagobi.meta.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///it/eng/spagobi/meta/model.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "it.eng.spagobi.meta.model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = it.eng.spagobi.meta.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl <em>Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelObjectImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelObject()
	 * @generated
	 */
	int MODEL_OBJECT = 4;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyTypeImpl <em>Property Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelPropertyTypeImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyType()
	 * @generated
	 */
	int MODEL_PROPERTY_TYPE = 1;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl <em>Property Category</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyCategory()
	 * @generated
	 */
	int MODEL_PROPERTY_CATEGORY = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY__NAME = 0;

	/**
	 * The feature id for the '<em><b>Parent Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY = 1;

	/**
	 * The feature id for the '<em><b>Sub Categories</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES = 2;

	/**
	 * The feature id for the '<em><b>Property Types</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES = 3;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY__DESCRIPTION = 4;

	/**
	 * The number of structural features of the '<em>Property Category</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_CATEGORY_FEATURE_COUNT = 5;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__ID = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__NAME = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__CATEGORY = 3;

	/**
	 * The feature id for the '<em><b>Admissible Values</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__ADMISSIBLE_VALUES = 4;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE__DEFAULT_VALUE = 5;

	/**
	 * The number of structural features of the '<em>Property Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_TYPE_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyImpl <em>Property</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelPropertyImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelProperty()
	 * @generated
	 */
	int MODEL_PROPERTY = 2;

	/**
	 * The feature id for the '<em><b>Property Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY__PROPERTY_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Property</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyMapEntryImpl <em>Property Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelPropertyMapEntryImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyMapEntry()
	 * @generated
	 */
	int MODEL_PROPERTY_MAP_ENTRY = 3;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Property Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_PROPERTY_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT__ID = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT__NAME = 1;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT__UNIQUE_NAME = 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT__DESCRIPTION = 3;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT__PROPERTIES = 4;

	/**
	 * The number of structural features of the '<em>Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_OBJECT_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.impl.ModelImpl <em>Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.impl.ModelImpl
	 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModel()
	 * @generated
	 */
	int MODEL = 5;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__ID = MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__NAME = MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__UNIQUE_NAME = MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__DESCRIPTION = MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__PROPERTIES = MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Physical Models</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__PHYSICAL_MODELS = MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Business Models</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__BUSINESS_MODELS = MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Olap Models</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__OLAP_MODELS = MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Property Types</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__PROPERTY_TYPES = MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Property Categories</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL__PROPERTY_CATEGORIES = MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_FEATURE_COUNT = MODEL_OBJECT_FEATURE_COUNT + 5;


	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.ModelObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject
	 * @generated
	 */
	EClass getModelObject();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelObject#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject#getId()
	 * @see #getModelObject()
	 * @generated
	 */
	EAttribute getModelObject_Id();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelObject#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject#getName()
	 * @see #getModelObject()
	 * @generated
	 */
	EAttribute getModelObject_Name();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelObject#getUniqueName <em>Unique Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unique Name</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject#getUniqueName()
	 * @see #getModelObject()
	 * @generated
	 */
	EAttribute getModelObject_UniqueName();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelObject#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject#getDescription()
	 * @see #getModelObject()
	 * @generated
	 */
	EAttribute getModelObject_Description();

	/**
	 * Returns the meta object for the map '{@link it.eng.spagobi.meta.model.ModelObject#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Properties</em>'.
	 * @see it.eng.spagobi.meta.model.ModelObject#getProperties()
	 * @see #getModelObject()
	 * @generated
	 */
	EReference getModelObject_Properties();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.ModelPropertyType <em>Property Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Property Type</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType
	 * @generated
	 */
	EClass getModelPropertyType();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyType#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getId()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EAttribute getModelPropertyType_Id();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyType#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getName()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EAttribute getModelPropertyType_Name();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyType#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getDescription()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EAttribute getModelPropertyType_Description();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.ModelPropertyType#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Category</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getCategory()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EReference getModelPropertyType_Category();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyType#getDefaultValue <em>Default Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Default Value</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getDefaultValue()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EAttribute getModelPropertyType_DefaultValue();

	/**
	 * Returns the meta object for the attribute list '{@link it.eng.spagobi.meta.model.ModelPropertyType#getAdmissibleValues <em>Admissible Values</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Admissible Values</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyType#getAdmissibleValues()
	 * @see #getModelPropertyType()
	 * @generated
	 */
	EAttribute getModelPropertyType_AdmissibleValues();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.ModelPropertyCategory <em>Property Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Property Category</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory
	 * @generated
	 */
	EClass getModelPropertyCategory();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyCategory#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory#getName()
	 * @see #getModelPropertyCategory()
	 * @generated
	 */
	EAttribute getModelPropertyCategory_Name();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelPropertyCategory#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory#getDescription()
	 * @see #getModelPropertyCategory()
	 * @generated
	 */
	EAttribute getModelPropertyCategory_Description();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.ModelPropertyCategory#getParentCategory <em>Parent Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Parent Category</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory#getParentCategory()
	 * @see #getModelPropertyCategory()
	 * @generated
	 */
	EReference getModelPropertyCategory_ParentCategory();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.ModelPropertyCategory#getSubCategories <em>Sub Categories</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Sub Categories</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory#getSubCategories()
	 * @see #getModelPropertyCategory()
	 * @generated
	 */
	EReference getModelPropertyCategory_SubCategories();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.ModelPropertyCategory#getPropertyTypes <em>Property Types</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Property Types</em>'.
	 * @see it.eng.spagobi.meta.model.ModelPropertyCategory#getPropertyTypes()
	 * @see #getModelPropertyCategory()
	 * @generated
	 */
	EReference getModelPropertyCategory_PropertyTypes();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.ModelProperty <em>Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Property</em>'.
	 * @see it.eng.spagobi.meta.model.ModelProperty
	 * @generated
	 */
	EClass getModelProperty();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.ModelProperty#getPropertyType <em>Property Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Property Type</em>'.
	 * @see it.eng.spagobi.meta.model.ModelProperty#getPropertyType()
	 * @see #getModelProperty()
	 * @generated
	 */
	EReference getModelProperty_PropertyType();

	/**
	 * Returns the meta object for the attribute '{@link it.eng.spagobi.meta.model.ModelProperty#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see it.eng.spagobi.meta.model.ModelProperty#getValue()
	 * @see #getModelProperty()
	 * @generated
	 */
	EAttribute getModelProperty_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Property Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Property Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueType="it.eng.spagobi.meta.model.ModelProperty" valueContainment="true"
	 * @generated
	 */
	EClass getModelPropertyMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getModelPropertyMapEntry()
	 * @generated
	 */
	EAttribute getModelPropertyMapEntry_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getModelPropertyMapEntry()
	 * @generated
	 */
	EReference getModelPropertyMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.Model <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.Model
	 * @generated
	 */
	EClass getModel();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.Model#getPhysicalModels <em>Physical Models</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Physical Models</em>'.
	 * @see it.eng.spagobi.meta.model.Model#getPhysicalModels()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_PhysicalModels();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.Model#getBusinessModels <em>Business Models</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Business Models</em>'.
	 * @see it.eng.spagobi.meta.model.Model#getBusinessModels()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_BusinessModels();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.Model#getOlapModels <em>Olap Models</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Olap Models</em>'.
	 * @see it.eng.spagobi.meta.model.Model#getOlapModels()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_OlapModels();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.Model#getPropertyTypes <em>Property Types</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Property Types</em>'.
	 * @see it.eng.spagobi.meta.model.Model#getPropertyTypes()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_PropertyTypes();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.Model#getPropertyCategories <em>Property Categories</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Property Categories</em>'.
	 * @see it.eng.spagobi.meta.model.Model#getPropertyCategories()
	 * @see #getModel()
	 * @generated
	 */
	EReference getModel_PropertyCategories();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

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
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl <em>Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelObjectImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelObject()
		 * @generated
		 */
		EClass MODEL_OBJECT = eINSTANCE.getModelObject();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_OBJECT__ID = eINSTANCE.getModelObject_Id();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_OBJECT__NAME = eINSTANCE.getModelObject_Name();

		/**
		 * The meta object literal for the '<em><b>Unique Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_OBJECT__UNIQUE_NAME = eINSTANCE.getModelObject_UniqueName();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_OBJECT__DESCRIPTION = eINSTANCE.getModelObject_Description();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_OBJECT__PROPERTIES = eINSTANCE.getModelObject_Properties();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyTypeImpl <em>Property Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelPropertyTypeImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyType()
		 * @generated
		 */
		EClass MODEL_PROPERTY_TYPE = eINSTANCE.getModelPropertyType();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_TYPE__ID = eINSTANCE.getModelPropertyType_Id();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_TYPE__NAME = eINSTANCE.getModelPropertyType_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_TYPE__DESCRIPTION = eINSTANCE.getModelPropertyType_Description();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY_TYPE__CATEGORY = eINSTANCE.getModelPropertyType_Category();

		/**
		 * The meta object literal for the '<em><b>Default Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_TYPE__DEFAULT_VALUE = eINSTANCE.getModelPropertyType_DefaultValue();

		/**
		 * The meta object literal for the '<em><b>Admissible Values</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_TYPE__ADMISSIBLE_VALUES = eINSTANCE.getModelPropertyType_AdmissibleValues();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl <em>Property Category</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyCategory()
		 * @generated
		 */
		EClass MODEL_PROPERTY_CATEGORY = eINSTANCE.getModelPropertyCategory();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_CATEGORY__NAME = eINSTANCE.getModelPropertyCategory_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_CATEGORY__DESCRIPTION = eINSTANCE.getModelPropertyCategory_Description();

		/**
		 * The meta object literal for the '<em><b>Parent Category</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY = eINSTANCE.getModelPropertyCategory_ParentCategory();

		/**
		 * The meta object literal for the '<em><b>Sub Categories</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES = eINSTANCE.getModelPropertyCategory_SubCategories();

		/**
		 * The meta object literal for the '<em><b>Property Types</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES = eINSTANCE.getModelPropertyCategory_PropertyTypes();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyImpl <em>Property</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelPropertyImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelProperty()
		 * @generated
		 */
		EClass MODEL_PROPERTY = eINSTANCE.getModelProperty();

		/**
		 * The meta object literal for the '<em><b>Property Type</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY__PROPERTY_TYPE = eINSTANCE.getModelProperty_PropertyType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY__VALUE = eINSTANCE.getModelProperty_Value();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelPropertyMapEntryImpl <em>Property Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelPropertyMapEntryImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModelPropertyMapEntry()
		 * @generated
		 */
		EClass MODEL_PROPERTY_MAP_ENTRY = eINSTANCE.getModelPropertyMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_PROPERTY_MAP_ENTRY__KEY = eINSTANCE.getModelPropertyMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_PROPERTY_MAP_ENTRY__VALUE = eINSTANCE.getModelPropertyMapEntry_Value();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.impl.ModelImpl <em>Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.impl.ModelImpl
		 * @see it.eng.spagobi.meta.model.impl.ModelPackageImpl#getModel()
		 * @generated
		 */
		EClass MODEL = eINSTANCE.getModel();

		/**
		 * The meta object literal for the '<em><b>Physical Models</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__PHYSICAL_MODELS = eINSTANCE.getModel_PhysicalModels();

		/**
		 * The meta object literal for the '<em><b>Business Models</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__BUSINESS_MODELS = eINSTANCE.getModel_BusinessModels();

		/**
		 * The meta object literal for the '<em><b>Olap Models</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__OLAP_MODELS = eINSTANCE.getModel_OlapModels();

		/**
		 * The meta object literal for the '<em><b>Property Types</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__PROPERTY_TYPES = eINSTANCE.getModel_PropertyTypes();

		/**
		 * The meta object literal for the '<em><b>Property Categories</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL__PROPERTY_CATEGORIES = eINSTANCE.getModel_PropertyCategories();

	}

} //ModelPackage
