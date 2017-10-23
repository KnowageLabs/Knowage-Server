/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.analytical.AnalyticalModelPackage;
import it.eng.spagobi.meta.model.analytical.impl.AnalyticalModelPackageImpl;
import it.eng.spagobi.meta.model.behavioural.BehaviouralModelPackage;
import it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelPackageImpl;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessDomain;
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessModelFactory;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.spagobi.meta.model.business.CalculatedBusinessColumn;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.impl.ModelPackageImpl;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;
import it.eng.spagobi.meta.model.olap.impl.OlapModelPackageImpl;
import it.eng.spagobi.meta.model.physical.PhysicalModelPackage;
import it.eng.spagobi.meta.model.physical.impl.PhysicalModelPackageImpl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class BusinessModelPackageImpl extends EPackageImpl implements BusinessModelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessTableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessColumnEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessColumnSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessRelationshipEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessViewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessDomainEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessIdentifierEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass businessViewInnerJoinRelationshipEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass simpleBusinessColumnEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass calculatedBusinessColumnEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private BusinessModelPackageImpl() {
		super(eNS_URI, BusinessModelFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link BusinessModelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static BusinessModelPackage init() {
		if (isInited) return (BusinessModelPackage)EPackage.Registry.INSTANCE.getEPackage(BusinessModelPackage.eNS_URI);

		// Obtain or create and register package
		BusinessModelPackageImpl theBusinessModelPackage = (BusinessModelPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof BusinessModelPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new BusinessModelPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ModelPackageImpl theModelPackage = (ModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI) instanceof ModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI) : ModelPackage.eINSTANCE);
		PhysicalModelPackageImpl thePhysicalModelPackage = (PhysicalModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(PhysicalModelPackage.eNS_URI) instanceof PhysicalModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(PhysicalModelPackage.eNS_URI) : PhysicalModelPackage.eINSTANCE);
		OlapModelPackageImpl theOlapModelPackage = (OlapModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(OlapModelPackage.eNS_URI) instanceof OlapModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(OlapModelPackage.eNS_URI) : OlapModelPackage.eINSTANCE);
		BehaviouralModelPackageImpl theBehaviouralModelPackage = (BehaviouralModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(BehaviouralModelPackage.eNS_URI) instanceof BehaviouralModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(BehaviouralModelPackage.eNS_URI) : BehaviouralModelPackage.eINSTANCE);
		AnalyticalModelPackageImpl theAnalyticalModelPackage = (AnalyticalModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(AnalyticalModelPackage.eNS_URI) instanceof AnalyticalModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(AnalyticalModelPackage.eNS_URI) : AnalyticalModelPackage.eINSTANCE);

		// Create package meta-data objects
		theBusinessModelPackage.createPackageContents();
		theModelPackage.createPackageContents();
		thePhysicalModelPackage.createPackageContents();
		theOlapModelPackage.createPackageContents();
		theBehaviouralModelPackage.createPackageContents();
		theAnalyticalModelPackage.createPackageContents();

		// Initialize created meta-data
		theBusinessModelPackage.initializePackageContents();
		theModelPackage.initializePackageContents();
		thePhysicalModelPackage.initializePackageContents();
		theOlapModelPackage.initializePackageContents();
		theBehaviouralModelPackage.initializePackageContents();
		theAnalyticalModelPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theBusinessModelPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(BusinessModelPackage.eNS_URI, theBusinessModelPackage);
		return theBusinessModelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessModel() {
		return businessModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_ParentModel() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_PhysicalModel() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_Tables() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_Relationships() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_Identifiers() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_Domains() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessModel_JoinRelationships() {
		return (EReference)businessModelEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessTable() {
		return businessTableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessTable_PhysicalTable() {
		return (EReference)businessTableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessColumn() {
		return businessColumnEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessColumn_Table() {
		return (EReference)businessColumnEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessColumnSet() {
		return businessColumnSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessColumnSet_Model() {
		return (EReference)businessColumnSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessColumnSet_Columns() {
		return (EReference)businessColumnSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessRelationship() {
		return businessRelationshipEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_Model() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_SourceTable() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_DestinationTable() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_SourceColumns() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_DestinationColumns() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessRelationship_PhysicalForeignKey() {
		return (EReference)businessRelationshipEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessView() {
		return businessViewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessView_JoinRelationships() {
		return (EReference)businessViewEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessDomain() {
		return businessDomainEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessDomain_Model() {
		return (EReference)businessDomainEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessDomain_Tables() {
		return (EReference)businessDomainEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessDomain_Relationships() {
		return (EReference)businessDomainEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessIdentifier() {
		return businessIdentifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessIdentifier_Model() {
		return (EReference)businessIdentifierEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessIdentifier_Table() {
		return (EReference)businessIdentifierEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessIdentifier_Columns() {
		return (EReference)businessIdentifierEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessIdentifier_PhysicalPrimaryKey() {
		return (EReference)businessIdentifierEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBusinessViewInnerJoinRelationship() {
		return businessViewInnerJoinRelationshipEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessViewInnerJoinRelationship_Model() {
		return (EReference)businessViewInnerJoinRelationshipEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessViewInnerJoinRelationship_SourceTable() {
		return (EReference)businessViewInnerJoinRelationshipEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessViewInnerJoinRelationship_DestinationTable() {
		return (EReference)businessViewInnerJoinRelationshipEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessViewInnerJoinRelationship_SourceColumns() {
		return (EReference)businessViewInnerJoinRelationshipEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBusinessViewInnerJoinRelationship_DestinationColumns() {
		return (EReference)businessViewInnerJoinRelationshipEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSimpleBusinessColumn() {
		return simpleBusinessColumnEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSimpleBusinessColumn_PhysicalColumn() {
		return (EReference)simpleBusinessColumnEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCalculatedBusinessColumn() {
		return calculatedBusinessColumnEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModelFactory getBusinessModelFactory() {
		return (BusinessModelFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		businessModelEClass = createEClass(BUSINESS_MODEL);
		createEReference(businessModelEClass, BUSINESS_MODEL__PARENT_MODEL);
		createEReference(businessModelEClass, BUSINESS_MODEL__PHYSICAL_MODEL);
		createEReference(businessModelEClass, BUSINESS_MODEL__TABLES);
		createEReference(businessModelEClass, BUSINESS_MODEL__RELATIONSHIPS);
		createEReference(businessModelEClass, BUSINESS_MODEL__IDENTIFIERS);
		createEReference(businessModelEClass, BUSINESS_MODEL__DOMAINS);
		createEReference(businessModelEClass, BUSINESS_MODEL__JOIN_RELATIONSHIPS);

		businessColumnEClass = createEClass(BUSINESS_COLUMN);
		createEReference(businessColumnEClass, BUSINESS_COLUMN__TABLE);

		businessColumnSetEClass = createEClass(BUSINESS_COLUMN_SET);
		createEReference(businessColumnSetEClass, BUSINESS_COLUMN_SET__MODEL);
		createEReference(businessColumnSetEClass, BUSINESS_COLUMN_SET__COLUMNS);

		businessTableEClass = createEClass(BUSINESS_TABLE);
		createEReference(businessTableEClass, BUSINESS_TABLE__PHYSICAL_TABLE);

		businessViewEClass = createEClass(BUSINESS_VIEW);
		createEReference(businessViewEClass, BUSINESS_VIEW__JOIN_RELATIONSHIPS);

		businessRelationshipEClass = createEClass(BUSINESS_RELATIONSHIP);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__MODEL);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__SOURCE_TABLE);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__DESTINATION_TABLE);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__SOURCE_COLUMNS);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS);
		createEReference(businessRelationshipEClass, BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY);

		businessDomainEClass = createEClass(BUSINESS_DOMAIN);
		createEReference(businessDomainEClass, BUSINESS_DOMAIN__MODEL);
		createEReference(businessDomainEClass, BUSINESS_DOMAIN__TABLES);
		createEReference(businessDomainEClass, BUSINESS_DOMAIN__RELATIONSHIPS);

		businessIdentifierEClass = createEClass(BUSINESS_IDENTIFIER);
		createEReference(businessIdentifierEClass, BUSINESS_IDENTIFIER__MODEL);
		createEReference(businessIdentifierEClass, BUSINESS_IDENTIFIER__TABLE);
		createEReference(businessIdentifierEClass, BUSINESS_IDENTIFIER__COLUMNS);
		createEReference(businessIdentifierEClass, BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY);

		businessViewInnerJoinRelationshipEClass = createEClass(BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP);
		createEReference(businessViewInnerJoinRelationshipEClass, BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__MODEL);
		createEReference(businessViewInnerJoinRelationshipEClass, BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_TABLE);
		createEReference(businessViewInnerJoinRelationshipEClass, BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_TABLE);
		createEReference(businessViewInnerJoinRelationshipEClass, BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__SOURCE_COLUMNS);
		createEReference(businessViewInnerJoinRelationshipEClass, BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP__DESTINATION_COLUMNS);

		simpleBusinessColumnEClass = createEClass(SIMPLE_BUSINESS_COLUMN);
		createEReference(simpleBusinessColumnEClass, SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN);

		calculatedBusinessColumnEClass = createEClass(CALCULATED_BUSINESS_COLUMN);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ModelPackage theModelPackage = (ModelPackage)EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI);
		PhysicalModelPackage thePhysicalModelPackage = (PhysicalModelPackage)EPackage.Registry.INSTANCE.getEPackage(PhysicalModelPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		businessModelEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessColumnEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessColumnSetEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessTableEClass.getESuperTypes().add(this.getBusinessColumnSet());
		businessViewEClass.getESuperTypes().add(this.getBusinessColumnSet());
		businessRelationshipEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessDomainEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessIdentifierEClass.getESuperTypes().add(theModelPackage.getModelObject());
		businessViewInnerJoinRelationshipEClass.getESuperTypes().add(theModelPackage.getModelObject());
		simpleBusinessColumnEClass.getESuperTypes().add(this.getBusinessColumn());
		calculatedBusinessColumnEClass.getESuperTypes().add(this.getBusinessColumn());

		// Initialize classes and features; add operations and parameters
		initEClass(businessModelEClass, BusinessModel.class, "BusinessModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessModel_ParentModel(), theModelPackage.getModel(), theModelPackage.getModel_BusinessModels(), "parentModel", null, 1, 1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_PhysicalModel(), thePhysicalModelPackage.getPhysicalModel(), null, "physicalModel", null, 1, 1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_Tables(), this.getBusinessColumnSet(), this.getBusinessColumnSet_Model(), "tables", null, 0, -1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_Relationships(), this.getBusinessRelationship(), this.getBusinessRelationship_Model(), "relationships", null, 0, -1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_Identifiers(), this.getBusinessIdentifier(), this.getBusinessIdentifier_Model(), "identifiers", null, 0, -1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_Domains(), this.getBusinessDomain(), this.getBusinessDomain_Model(), "domains", null, 0, -1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessModel_JoinRelationships(), this.getBusinessViewInnerJoinRelationship(), this.getBusinessViewInnerJoinRelationship_Model(), "joinRelationships", null, 0, -1, BusinessModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessColumnEClass, BusinessColumn.class, "BusinessColumn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessColumn_Table(), this.getBusinessColumnSet(), this.getBusinessColumnSet_Columns(), "table", null, 1, 1, BusinessColumn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessColumnSetEClass, BusinessColumnSet.class, "BusinessColumnSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessColumnSet_Model(), this.getBusinessModel(), this.getBusinessModel_Tables(), "model", null, 1, 1, BusinessColumnSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessColumnSet_Columns(), this.getBusinessColumn(), this.getBusinessColumn_Table(), "columns", null, 0, -1, BusinessColumnSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessTableEClass, BusinessTable.class, "BusinessTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessTable_PhysicalTable(), thePhysicalModelPackage.getPhysicalTable(), null, "physicalTable", null, 1, 1, BusinessTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessViewEClass, BusinessView.class, "BusinessView", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessView_JoinRelationships(), this.getBusinessViewInnerJoinRelationship(), null, "joinRelationships", null, 0, -1, BusinessView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessRelationshipEClass, BusinessRelationship.class, "BusinessRelationship", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessRelationship_Model(), this.getBusinessModel(), this.getBusinessModel_Relationships(), "model", null, 1, 1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessRelationship_SourceTable(), this.getBusinessColumnSet(), null, "sourceTable", null, 1, 1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessRelationship_DestinationTable(), this.getBusinessColumnSet(), null, "destinationTable", null, 1, 1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessRelationship_SourceColumns(), this.getBusinessColumn(), null, "sourceColumns", null, 0, -1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessRelationship_DestinationColumns(), this.getBusinessColumn(), null, "destinationColumns", null, 0, -1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessRelationship_PhysicalForeignKey(), thePhysicalModelPackage.getPhysicalForeignKey(), null, "physicalForeignKey", null, 0, 1, BusinessRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessDomainEClass, BusinessDomain.class, "BusinessDomain", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessDomain_Model(), this.getBusinessModel(), this.getBusinessModel_Domains(), "model", null, 1, 1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessDomain_Tables(), this.getBusinessColumnSet(), null, "tables", null, 0, -1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessDomain_Relationships(), this.getBusinessRelationship(), null, "relationships", null, 0, -1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessIdentifierEClass, BusinessIdentifier.class, "BusinessIdentifier", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessIdentifier_Model(), this.getBusinessModel(), this.getBusinessModel_Identifiers(), "model", null, 0, 1, BusinessIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessIdentifier_Table(), this.getBusinessColumnSet(), null, "table", null, 1, 1, BusinessIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessIdentifier_Columns(), this.getBusinessColumn(), null, "columns", null, 1, -1, BusinessIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessIdentifier_PhysicalPrimaryKey(), thePhysicalModelPackage.getPhysicalPrimaryKey(), null, "physicalPrimaryKey", null, 0, 1, BusinessIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(businessViewInnerJoinRelationshipEClass, BusinessViewInnerJoinRelationship.class, "BusinessViewInnerJoinRelationship", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBusinessViewInnerJoinRelationship_Model(), this.getBusinessModel(), this.getBusinessModel_JoinRelationships(), "model", null, 1, 1, BusinessViewInnerJoinRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessViewInnerJoinRelationship_SourceTable(), thePhysicalModelPackage.getPhysicalTable(), null, "sourceTable", null, 1, 1, BusinessViewInnerJoinRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessViewInnerJoinRelationship_DestinationTable(), thePhysicalModelPackage.getPhysicalTable(), null, "destinationTable", null, 1, 1, BusinessViewInnerJoinRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessViewInnerJoinRelationship_SourceColumns(), thePhysicalModelPackage.getPhysicalColumn(), null, "sourceColumns", null, 0, -1, BusinessViewInnerJoinRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBusinessViewInnerJoinRelationship_DestinationColumns(), thePhysicalModelPackage.getPhysicalColumn(), null, "destinationColumns", null, 0, -1, BusinessViewInnerJoinRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(simpleBusinessColumnEClass, SimpleBusinessColumn.class, "SimpleBusinessColumn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSimpleBusinessColumn_PhysicalColumn(), thePhysicalModelPackage.getPhysicalColumn(), null, "physicalColumn", null, 1, 1, SimpleBusinessColumn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(calculatedBusinessColumnEClass, CalculatedBusinessColumn.class, "CalculatedBusinessColumn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
	}

} //BusinessModelPackageImpl
