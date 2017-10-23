/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.meta.model.business.*;
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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class BusinessModelFactoryImpl extends EFactoryImpl implements BusinessModelFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BusinessModelFactory init() {
		try {
			BusinessModelFactory theBusinessModelFactory = (BusinessModelFactory)EPackage.Registry.INSTANCE.getEFactory("http:///it/eng/spagobi/meta/model/businessl.ecore"); 
			if (theBusinessModelFactory != null) {
				return theBusinessModelFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new BusinessModelFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModelFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case BusinessModelPackage.BUSINESS_MODEL: return createBusinessModel();
			case BusinessModelPackage.BUSINESS_COLUMN: return createBusinessColumn();
			case BusinessModelPackage.BUSINESS_COLUMN_SET: return createBusinessColumnSet();
			case BusinessModelPackage.BUSINESS_TABLE: return createBusinessTable();
			case BusinessModelPackage.BUSINESS_VIEW: return createBusinessView();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP: return createBusinessRelationship();
			case BusinessModelPackage.BUSINESS_DOMAIN: return createBusinessDomain();
			case BusinessModelPackage.BUSINESS_IDENTIFIER: return createBusinessIdentifier();
			case BusinessModelPackage.BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP: return createBusinessViewInnerJoinRelationship();
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN: return createSimpleBusinessColumn();
			case BusinessModelPackage.CALCULATED_BUSINESS_COLUMN: return createCalculatedBusinessColumn();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModel createBusinessModel() {
		BusinessModelImpl businessModel = new BusinessModelImpl();
		return businessModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessTable createBusinessTable() {
		BusinessTableImpl businessTable = new BusinessTableImpl();
		return businessTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn createBusinessColumn() {
		BusinessColumnImpl businessColumn = new BusinessColumnImpl();
		return businessColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet createBusinessColumnSet() {
		BusinessColumnSetImpl businessColumnSet = new BusinessColumnSetImpl();
		return businessColumnSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessRelationship createBusinessRelationship() {
		BusinessRelationshipImpl businessRelationship = new BusinessRelationshipImpl();
		return businessRelationship;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessView createBusinessView() {
		BusinessViewImpl businessView = new BusinessViewImpl();
		return businessView;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessDomain createBusinessDomain() {
		BusinessDomainImpl businessDomain = new BusinessDomainImpl();
		return businessDomain;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessIdentifier createBusinessIdentifier() {
		BusinessIdentifierImpl businessIdentifier = new BusinessIdentifierImpl();
		return businessIdentifier;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessViewInnerJoinRelationship createBusinessViewInnerJoinRelationship() {
		BusinessViewInnerJoinRelationshipImpl businessViewInnerJoinRelationship = new BusinessViewInnerJoinRelationshipImpl();
		return businessViewInnerJoinRelationship;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SimpleBusinessColumn createSimpleBusinessColumn() {
		SimpleBusinessColumnImpl simpleBusinessColumn = new SimpleBusinessColumnImpl();
		return simpleBusinessColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CalculatedBusinessColumn createCalculatedBusinessColumn() {
		CalculatedBusinessColumnImpl calculatedBusinessColumn = new CalculatedBusinessColumnImpl();
		return calculatedBusinessColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModelPackage getBusinessModelPackage() {
		return (BusinessModelPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static BusinessModelPackage getPackage() {
		return BusinessModelPackage.eINSTANCE;
	}

} //BusinessModelFactoryImpl
