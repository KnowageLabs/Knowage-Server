/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.model.business.impl;

import it.eng.knowage.meta.model.business.*;

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
