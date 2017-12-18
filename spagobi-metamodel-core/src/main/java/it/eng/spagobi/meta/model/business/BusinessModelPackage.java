/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

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
 * @see it.eng.spagobi.meta.model.business.BusinessModelFactory
 * @model kind="package"
 * @generated
 */
public interface BusinessModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "business";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///it/eng/spagobi/meta/model/businessl.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "it.eng.spagobi.meta.model.business";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BusinessModelPackage eINSTANCE = it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessModelImpl <em>Business Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessModel()
	 * @generated
	 */
	int BUSINESS_MODEL = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Parent Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__PARENT_MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Physical Model</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__PHYSICAL_MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tables</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__TABLES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Relationships</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__RELATIONSHIPS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Identifiers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__IDENTIFIERS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Domains</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__DOMAINS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Join Relationships</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL__JOIN_RELATIONSHIPS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Business Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_MODEL_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 7;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessTableImpl <em>Business Table</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessTableImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessTable()
	 * @generated
	 */
	int BUSINESS_TABLE = 3;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnImpl <em>Business Column</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessColumnImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessColumn()
	 * @generated
	 */
	int BUSINESS_COLUMN = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Table</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN__TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Business Column</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl <em>Business Column Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessColumnSet()
	 * @generated
	 */
	int BUSINESS_COLUMN_SET = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Columns</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET__COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Business Column Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_COLUMN_SET_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__ID = BUSINESS_COLUMN_SET__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__NAME = BUSINESS_COLUMN_SET__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__UNIQUE_NAME = BUSINESS_COLUMN_SET__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__DESCRIPTION = BUSINESS_COLUMN_SET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__PROPERTIES = BUSINESS_COLUMN_SET__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__MODEL = BUSINESS_COLUMN_SET__MODEL;

	/**
	 * The feature id for the '<em><b>Columns</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__COLUMNS = BUSINESS_COLUMN_SET__COLUMNS;

	/**
	 * The feature id for the '<em><b>Physical Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE__PHYSICAL_TABLE = BUSINESS_COLUMN_SET_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Business Table</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_TABLE_FEATURE_COUNT = BUSINESS_COLUMN_SET_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl <em>Business Relationship</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessRelationship()
	 * @generated
	 */
	int BUSINESS_RELATIONSHIP = 5;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessViewImpl <em>Business View</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessViewImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessView()
	 * @generated
	 */
	int BUSINESS_VIEW = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__ID = BUSINESS_COLUMN_SET__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__NAME = BUSINESS_COLUMN_SET__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__UNIQUE_NAME = BUSINESS_COLUMN_SET__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__DESCRIPTION = BUSINESS_COLUMN_SET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__PROPERTIES = BUSINESS_COLUMN_SET__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__MODEL = BUSINESS_COLUMN_SET__MODEL;

	/**
	 * The feature id for the '<em><b>Columns</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__COLUMNS = BUSINESS_COLUMN_SET__COLUMNS;

	/**
	 * The feature id for the '<em><b>Join Relationships</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW__JOIN_RELATIONSHIPS = BUSINESS_COLUMN_SET_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Business View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_FEATURE_COUNT = BUSINESS_COLUMN_SET_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Source Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__SOURCE_TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Destination Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__DESTINATION_TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Source Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__SOURCE_COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Destination Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Physical Foreign Key</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Business Relationship</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_RELATIONSHIP_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessDomainImpl <em>Business Domain</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessDomainImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessDomain()
	 * @generated
	 */
	int BUSINESS_DOMAIN = 6;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Tables</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__TABLES = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Relationships</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN__RELATIONSHIPS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Business Domain</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_DOMAIN_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;


	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessIdentifierImpl <em>Business Identifier</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessIdentifierImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessIdentifier()
	 * @generated
	 */
	int BUSINESS_IDENTIFIER = 7;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Physical Primary Key</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Business Identifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_IDENTIFIER_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;


	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessViewInnerJoinRelationshipImpl <em>Business View Inner Join Relationship</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessViewInnerJoinRelationshipImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP = 8;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__ID = ModelPackage.MODEL_OBJECT__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__NAME = ModelPackage.MODEL_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__UNIQUE_NAME = ModelPackage.MODEL_OBJECT__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESCRIPTION = ModelPackage.MODEL_OBJECT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__PROPERTIES = ModelPackage.MODEL_OBJECT__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Model</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__MODEL = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Source Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Destination Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_TABLE = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Source Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Destination Columns</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_COLUMNS = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Business View Inner Join Relationship</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP_FEATURE_COUNT = ModelPackage.MODEL_OBJECT_FEATURE_COUNT + 5;


	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.SimpleBusinessColumnImpl <em>Simple Business Column</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.SimpleBusinessColumnImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getSimpleBusinessColumn()
	 * @generated
	 */
	int SIMPLE_BUSINESS_COLUMN = 9;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__ID = BUSINESS_COLUMN__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__NAME = BUSINESS_COLUMN__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__UNIQUE_NAME = BUSINESS_COLUMN__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__DESCRIPTION = BUSINESS_COLUMN__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__PROPERTIES = BUSINESS_COLUMN__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Table</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__TABLE = BUSINESS_COLUMN__TABLE;

	/**
	 * The feature id for the '<em><b>Physical Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN = BUSINESS_COLUMN_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Simple Business Column</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_BUSINESS_COLUMN_FEATURE_COUNT = BUSINESS_COLUMN_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.business.impl.CalculatedBusinessColumnImpl <em>Calculated Business Column</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.business.impl.CalculatedBusinessColumnImpl
	 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getCalculatedBusinessColumn()
	 * @generated
	 */
	int CALCULATED_BUSINESS_COLUMN = 10;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__ID = BUSINESS_COLUMN__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__NAME = BUSINESS_COLUMN__NAME;

	/**
	 * The feature id for the '<em><b>Unique Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__UNIQUE_NAME = BUSINESS_COLUMN__UNIQUE_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__DESCRIPTION = BUSINESS_COLUMN__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__PROPERTIES = BUSINESS_COLUMN__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Table</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN__TABLE = BUSINESS_COLUMN__TABLE;

	/**
	 * The number of structural features of the '<em>Calculated Business Column</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALCULATED_BUSINESS_COLUMN_FEATURE_COUNT = BUSINESS_COLUMN_FEATURE_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessModel <em>Business Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel
	 * @generated
	 */
	EClass getBusinessModel();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessModel#getParentModel <em>Parent Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getParentModel()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_ParentModel();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessModel#getPhysicalModel <em>Physical Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Physical Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getPhysicalModel()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_PhysicalModel();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessModel#getTables <em>Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Tables</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getTables()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_Tables();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessModel#getRelationships <em>Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Relationships</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getRelationships()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_Relationships();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessModel#getIdentifiers <em>Identifiers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Identifiers</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getIdentifiers()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_Identifiers();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessModel#getDomains <em>Domains</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Domains</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getDomains()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_Domains();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessModel#getJoinRelationships <em>Join Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Join Relationships</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getJoinRelationships()
	 * @see #getBusinessModel()
	 * @generated
	 */
	EReference getBusinessModel_JoinRelationships();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessTable <em>Business Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessTable
	 * @generated
	 */
	EClass getBusinessTable();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessTable#getPhysicalTable <em>Physical Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Physical Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessTable#getPhysicalTable()
	 * @see #getBusinessTable()
	 * @generated
	 */
	EReference getBusinessTable_PhysicalTable();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessColumn <em>Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Column</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumn
	 * @generated
	 */
	EClass getBusinessColumn();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessColumn#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumn#getTable()
	 * @see #getBusinessColumn()
	 * @generated
	 */
	EReference getBusinessColumn_Table();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessColumnSet <em>Business Column Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Column Set</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumnSet
	 * @generated
	 */
	EClass getBusinessColumnSet();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumnSet#getModel()
	 * @see #getBusinessColumnSet()
	 * @generated
	 */
	EReference getBusinessColumnSet_Model();

	/**
	 * Returns the meta object for the containment reference list '{@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getColumns <em>Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumnSet#getColumns()
	 * @see #getBusinessColumnSet()
	 * @generated
	 */
	EReference getBusinessColumnSet_Columns();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessRelationship <em>Business Relationship</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Relationship</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship
	 * @generated
	 */
	EClass getBusinessRelationship();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getModel()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_Model();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getSourceTable <em>Source Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getSourceTable()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_SourceTable();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getDestinationTable <em>Destination Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Destination Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getDestinationTable()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_DestinationTable();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getSourceColumns <em>Source Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Source Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getSourceColumns()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_SourceColumns();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getDestinationColumns <em>Destination Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Destination Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getDestinationColumns()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_DestinationColumns();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessRelationship#getPhysicalForeignKey <em>Physical Foreign Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Physical Foreign Key</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship#getPhysicalForeignKey()
	 * @see #getBusinessRelationship()
	 * @generated
	 */
	EReference getBusinessRelationship_PhysicalForeignKey();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessView <em>Business View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business View</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessView
	 * @generated
	 */
	EClass getBusinessView();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessView#getJoinRelationships <em>Join Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Join Relationships</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessView#getJoinRelationships()
	 * @see #getBusinessView()
	 * @generated
	 */
	EReference getBusinessView_JoinRelationships();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessDomain <em>Business Domain</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Domain</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessDomain
	 * @generated
	 */
	EClass getBusinessDomain();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessDomain#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessDomain#getModel()
	 * @see #getBusinessDomain()
	 * @generated
	 */
	EReference getBusinessDomain_Model();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessDomain#getTables <em>Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Tables</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessDomain#getTables()
	 * @see #getBusinessDomain()
	 * @generated
	 */
	EReference getBusinessDomain_Tables();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessDomain#getRelationships <em>Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Relationships</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessDomain#getRelationships()
	 * @see #getBusinessDomain()
	 * @generated
	 */
	EReference getBusinessDomain_Relationships();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier <em>Business Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business Identifier</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier
	 * @generated
	 */
	EClass getBusinessIdentifier();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier#getModel()
	 * @see #getBusinessIdentifier()
	 * @generated
	 */
	EReference getBusinessIdentifier_Model();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier#getTable()
	 * @see #getBusinessIdentifier()
	 * @generated
	 */
	EReference getBusinessIdentifier_Table();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getColumns <em>Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier#getColumns()
	 * @see #getBusinessIdentifier()
	 * @generated
	 */
	EReference getBusinessIdentifier_Columns();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey <em>Physical Primary Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Physical Primary Key</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey()
	 * @see #getBusinessIdentifier()
	 * @generated
	 */
	EReference getBusinessIdentifier_PhysicalPrimaryKey();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship <em>Business View Inner Join Relationship</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Business View Inner Join Relationship</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship
	 * @generated
	 */
	EClass getBusinessViewInnerJoinRelationship();

	/**
	 * Returns the meta object for the container reference '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Model</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getModel()
	 * @see #getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	EReference getBusinessViewInnerJoinRelationship_Model();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable <em>Source Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable()
	 * @see #getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	EReference getBusinessViewInnerJoinRelationship_SourceTable();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable <em>Destination Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Destination Table</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable()
	 * @see #getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	EReference getBusinessViewInnerJoinRelationship_DestinationTable();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceColumns <em>Source Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Source Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceColumns()
	 * @see #getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	EReference getBusinessViewInnerJoinRelationship_SourceColumns();

	/**
	 * Returns the meta object for the reference list '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationColumns <em>Destination Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Destination Columns</em>'.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationColumns()
	 * @see #getBusinessViewInnerJoinRelationship()
	 * @generated
	 */
	EReference getBusinessViewInnerJoinRelationship_DestinationColumns();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.SimpleBusinessColumn <em>Simple Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Simple Business Column</em>'.
	 * @see it.eng.spagobi.meta.model.business.SimpleBusinessColumn
	 * @generated
	 */
	EClass getSimpleBusinessColumn();

	/**
	 * Returns the meta object for the reference '{@link it.eng.spagobi.meta.model.business.SimpleBusinessColumn#getPhysicalColumn <em>Physical Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Physical Column</em>'.
	 * @see it.eng.spagobi.meta.model.business.SimpleBusinessColumn#getPhysicalColumn()
	 * @see #getSimpleBusinessColumn()
	 * @generated
	 */
	EReference getSimpleBusinessColumn_PhysicalColumn();

	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.business.CalculatedBusinessColumn <em>Calculated Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Calculated Business Column</em>'.
	 * @see it.eng.spagobi.meta.model.business.CalculatedBusinessColumn
	 * @generated
	 */
	EClass getCalculatedBusinessColumn();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	BusinessModelFactory getBusinessModelFactory();

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
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessModelImpl <em>Business Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessModel()
		 * @generated
		 */
		EClass BUSINESS_MODEL = eINSTANCE.getBusinessModel();

		/**
		 * The meta object literal for the '<em><b>Parent Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__PARENT_MODEL = eINSTANCE.getBusinessModel_ParentModel();

		/**
		 * The meta object literal for the '<em><b>Physical Model</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__PHYSICAL_MODEL = eINSTANCE.getBusinessModel_PhysicalModel();

		/**
		 * The meta object literal for the '<em><b>Tables</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__TABLES = eINSTANCE.getBusinessModel_Tables();

		/**
		 * The meta object literal for the '<em><b>Relationships</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__RELATIONSHIPS = eINSTANCE.getBusinessModel_Relationships();

		/**
		 * The meta object literal for the '<em><b>Identifiers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__IDENTIFIERS = eINSTANCE.getBusinessModel_Identifiers();

		/**
		 * The meta object literal for the '<em><b>Domains</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__DOMAINS = eINSTANCE.getBusinessModel_Domains();

		/**
		 * The meta object literal for the '<em><b>Join Relationships</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_MODEL__JOIN_RELATIONSHIPS = eINSTANCE.getBusinessModel_JoinRelationships();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessTableImpl <em>Business Table</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessTableImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessTable()
		 * @generated
		 */
		EClass BUSINESS_TABLE = eINSTANCE.getBusinessTable();

		/**
		 * The meta object literal for the '<em><b>Physical Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_TABLE__PHYSICAL_TABLE = eINSTANCE.getBusinessTable_PhysicalTable();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnImpl <em>Business Column</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessColumnImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessColumn()
		 * @generated
		 */
		EClass BUSINESS_COLUMN = eINSTANCE.getBusinessColumn();

		/**
		 * The meta object literal for the '<em><b>Table</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_COLUMN__TABLE = eINSTANCE.getBusinessColumn_Table();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl <em>Business Column Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessColumnSet()
		 * @generated
		 */
		EClass BUSINESS_COLUMN_SET = eINSTANCE.getBusinessColumnSet();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_COLUMN_SET__MODEL = eINSTANCE.getBusinessColumnSet_Model();

		/**
		 * The meta object literal for the '<em><b>Columns</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_COLUMN_SET__COLUMNS = eINSTANCE.getBusinessColumnSet_Columns();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl <em>Business Relationship</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessRelationship()
		 * @generated
		 */
		EClass BUSINESS_RELATIONSHIP = eINSTANCE.getBusinessRelationship();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__MODEL = eINSTANCE.getBusinessRelationship_Model();

		/**
		 * The meta object literal for the '<em><b>Source Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__SOURCE_TABLE = eINSTANCE.getBusinessRelationship_SourceTable();

		/**
		 * The meta object literal for the '<em><b>Destination Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__DESTINATION_TABLE = eINSTANCE.getBusinessRelationship_DestinationTable();

		/**
		 * The meta object literal for the '<em><b>Source Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__SOURCE_COLUMNS = eINSTANCE.getBusinessRelationship_SourceColumns();

		/**
		 * The meta object literal for the '<em><b>Destination Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS = eINSTANCE.getBusinessRelationship_DestinationColumns();

		/**
		 * The meta object literal for the '<em><b>Physical Foreign Key</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY = eINSTANCE.getBusinessRelationship_PhysicalForeignKey();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessViewImpl <em>Business View</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessViewImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessView()
		 * @generated
		 */
		EClass BUSINESS_VIEW = eINSTANCE.getBusinessView();

		/**
		 * The meta object literal for the '<em><b>Join Relationships</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW__JOIN_RELATIONSHIPS = eINSTANCE.getBusinessView_JoinRelationships();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessDomainImpl <em>Business Domain</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessDomainImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessDomain()
		 * @generated
		 */
		EClass BUSINESS_DOMAIN = eINSTANCE.getBusinessDomain();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_DOMAIN__MODEL = eINSTANCE.getBusinessDomain_Model();

		/**
		 * The meta object literal for the '<em><b>Tables</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_DOMAIN__TABLES = eINSTANCE.getBusinessDomain_Tables();

		/**
		 * The meta object literal for the '<em><b>Relationships</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_DOMAIN__RELATIONSHIPS = eINSTANCE.getBusinessDomain_Relationships();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessIdentifierImpl <em>Business Identifier</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessIdentifierImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessIdentifier()
		 * @generated
		 */
		EClass BUSINESS_IDENTIFIER = eINSTANCE.getBusinessIdentifier();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_IDENTIFIER__MODEL = eINSTANCE.getBusinessIdentifier_Model();

		/**
		 * The meta object literal for the '<em><b>Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_IDENTIFIER__TABLE = eINSTANCE.getBusinessIdentifier_Table();

		/**
		 * The meta object literal for the '<em><b>Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_IDENTIFIER__COLUMNS = eINSTANCE.getBusinessIdentifier_Columns();

		/**
		 * The meta object literal for the '<em><b>Physical Primary Key</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY = eINSTANCE.getBusinessIdentifier_PhysicalPrimaryKey();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.BusinessViewInnerJoinRelationshipImpl <em>Business View Inner Join Relationship</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessViewInnerJoinRelationshipImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getBusinessViewInnerJoinRelationship()
		 * @generated
		 */
		EClass BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP = eINSTANCE.getBusinessViewInnerJoinRelationship();

		/**
		 * The meta object literal for the '<em><b>Model</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__MODEL = eINSTANCE.getBusinessViewInnerJoinRelationship_Model();

		/**
		 * The meta object literal for the '<em><b>Source Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_TABLE = eINSTANCE.getBusinessViewInnerJoinRelationship_SourceTable();

		/**
		 * The meta object literal for the '<em><b>Destination Table</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_TABLE = eINSTANCE.getBusinessViewInnerJoinRelationship_DestinationTable();

		/**
		 * The meta object literal for the '<em><b>Source Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_COLUMNS = eINSTANCE.getBusinessViewInnerJoinRelationship_SourceColumns();

		/**
		 * The meta object literal for the '<em><b>Destination Columns</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_COLUMNS = eINSTANCE.getBusinessViewInnerJoinRelationship_DestinationColumns();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.SimpleBusinessColumnImpl <em>Simple Business Column</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.SimpleBusinessColumnImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getSimpleBusinessColumn()
		 * @generated
		 */
		EClass SIMPLE_BUSINESS_COLUMN = eINSTANCE.getSimpleBusinessColumn();

		/**
		 * The meta object literal for the '<em><b>Physical Column</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN = eINSTANCE.getSimpleBusinessColumn_PhysicalColumn();

		/**
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.business.impl.CalculatedBusinessColumnImpl <em>Calculated Business Column</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.business.impl.CalculatedBusinessColumnImpl
		 * @see it.eng.spagobi.meta.model.business.impl.BusinessModelPackageImpl#getCalculatedBusinessColumn()
		 * @generated
		 */
		EClass CALCULATED_BUSINESS_COLUMN = eINSTANCE.getCalculatedBusinessColumn();

	}

} //BusinessModelPackage
