/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.util;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.business.*;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessDomain;
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.business.BusinessView;
import it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.spagobi.meta.model.business.CalculatedBusinessColumn;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage
 * @generated
 */
public class BusinessModelAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static BusinessModelPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModelAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = BusinessModelPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BusinessModelSwitch<Adapter> modelSwitch =
		new BusinessModelSwitch<Adapter>() {
			@Override
			public Adapter caseBusinessModel(BusinessModel object) {
				return createBusinessModelAdapter();
			}
			@Override
			public Adapter caseBusinessColumn(BusinessColumn object) {
				return createBusinessColumnAdapter();
			}
			@Override
			public Adapter caseBusinessColumnSet(BusinessColumnSet object) {
				return createBusinessColumnSetAdapter();
			}
			@Override
			public Adapter caseBusinessTable(BusinessTable object) {
				return createBusinessTableAdapter();
			}
			@Override
			public Adapter caseBusinessView(BusinessView object) {
				return createBusinessViewAdapter();
			}
			@Override
			public Adapter caseBusinessRelationship(BusinessRelationship object) {
				return createBusinessRelationshipAdapter();
			}
			@Override
			public Adapter caseBusinessDomain(BusinessDomain object) {
				return createBusinessDomainAdapter();
			}
			@Override
			public Adapter caseBusinessIdentifier(BusinessIdentifier object) {
				return createBusinessIdentifierAdapter();
			}
			@Override
			public Adapter caseBusinessViewInnerJoinRelationship(BusinessViewInnerJoinRelationship object) {
				return createBusinessViewInnerJoinRelationshipAdapter();
			}
			@Override
			public Adapter caseSimpleBusinessColumn(SimpleBusinessColumn object) {
				return createSimpleBusinessColumnAdapter();
			}
			@Override
			public Adapter caseCalculatedBusinessColumn(CalculatedBusinessColumn object) {
				return createCalculatedBusinessColumnAdapter();
			}
			@Override
			public Adapter caseModelObject(ModelObject object) {
				return createModelObjectAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessModel <em>Business Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessModel
	 * @generated
	 */
	public Adapter createBusinessModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessTable <em>Business Table</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessTable
	 * @generated
	 */
	public Adapter createBusinessTableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessColumn <em>Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumn
	 * @generated
	 */
	public Adapter createBusinessColumnAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessColumnSet <em>Business Column Set</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessColumnSet
	 * @generated
	 */
	public Adapter createBusinessColumnSetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessRelationship <em>Business Relationship</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessRelationship
	 * @generated
	 */
	public Adapter createBusinessRelationshipAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessView <em>Business View</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessView
	 * @generated
	 */
	public Adapter createBusinessViewAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessDomain <em>Business Domain</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessDomain
	 * @generated
	 */
	public Adapter createBusinessDomainAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier <em>Business Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessIdentifier
	 * @generated
	 */
	public Adapter createBusinessIdentifierAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship <em>Business View Inner Join Relationship</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship
	 * @generated
	 */
	public Adapter createBusinessViewInnerJoinRelationshipAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.SimpleBusinessColumn <em>Simple Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.SimpleBusinessColumn
	 * @generated
	 */
	public Adapter createSimpleBusinessColumnAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.business.CalculatedBusinessColumn <em>Calculated Business Column</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.business.CalculatedBusinessColumn
	 * @generated
	 */
	public Adapter createCalculatedBusinessColumnAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.ModelObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.ModelObject
	 * @generated
	 */
	public Adapter createModelObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //BusinessModelAdapterFactory
