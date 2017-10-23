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

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage
 * @generated
 */
public class BusinessModelSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static BusinessModelPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModelSwitch() {
		if (modelPackage == null) {
			modelPackage = BusinessModelPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case BusinessModelPackage.BUSINESS_MODEL: {
				BusinessModel businessModel = (BusinessModel)theEObject;
				T result = caseBusinessModel(businessModel);
				if (result == null) result = caseModelObject(businessModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_COLUMN: {
				BusinessColumn businessColumn = (BusinessColumn)theEObject;
				T result = caseBusinessColumn(businessColumn);
				if (result == null) result = caseModelObject(businessColumn);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_COLUMN_SET: {
				BusinessColumnSet businessColumnSet = (BusinessColumnSet)theEObject;
				T result = caseBusinessColumnSet(businessColumnSet);
				if (result == null) result = caseModelObject(businessColumnSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_TABLE: {
				BusinessTable businessTable = (BusinessTable)theEObject;
				T result = caseBusinessTable(businessTable);
				if (result == null) result = caseBusinessColumnSet(businessTable);
				if (result == null) result = caseModelObject(businessTable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_VIEW: {
				BusinessView businessView = (BusinessView)theEObject;
				T result = caseBusinessView(businessView);
				if (result == null) result = caseBusinessColumnSet(businessView);
				if (result == null) result = caseModelObject(businessView);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_RELATIONSHIP: {
				BusinessRelationship businessRelationship = (BusinessRelationship)theEObject;
				T result = caseBusinessRelationship(businessRelationship);
				if (result == null) result = caseModelObject(businessRelationship);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_DOMAIN: {
				BusinessDomain businessDomain = (BusinessDomain)theEObject;
				T result = caseBusinessDomain(businessDomain);
				if (result == null) result = caseModelObject(businessDomain);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_IDENTIFIER: {
				BusinessIdentifier businessIdentifier = (BusinessIdentifier)theEObject;
				T result = caseBusinessIdentifier(businessIdentifier);
				if (result == null) result = caseModelObject(businessIdentifier);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.BUSINESS_VIEW_INNER_JOIN_RELATIONSHIP: {
				BusinessViewInnerJoinRelationship businessViewInnerJoinRelationship = (BusinessViewInnerJoinRelationship)theEObject;
				T result = caseBusinessViewInnerJoinRelationship(businessViewInnerJoinRelationship);
				if (result == null) result = caseModelObject(businessViewInnerJoinRelationship);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN: {
				SimpleBusinessColumn simpleBusinessColumn = (SimpleBusinessColumn)theEObject;
				T result = caseSimpleBusinessColumn(simpleBusinessColumn);
				if (result == null) result = caseBusinessColumn(simpleBusinessColumn);
				if (result == null) result = caseModelObject(simpleBusinessColumn);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case BusinessModelPackage.CALCULATED_BUSINESS_COLUMN: {
				CalculatedBusinessColumn calculatedBusinessColumn = (CalculatedBusinessColumn)theEObject;
				T result = caseCalculatedBusinessColumn(calculatedBusinessColumn);
				if (result == null) result = caseBusinessColumn(calculatedBusinessColumn);
				if (result == null) result = caseModelObject(calculatedBusinessColumn);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessModel(BusinessModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Table</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Table</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessTable(BusinessTable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Column</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessColumn(BusinessColumn object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Column Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Column Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessColumnSet(BusinessColumnSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Relationship</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Relationship</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessRelationship(BusinessRelationship object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business View</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business View</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessView(BusinessView object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Domain</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Domain</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessDomain(BusinessDomain object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business Identifier</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business Identifier</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessIdentifier(BusinessIdentifier object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Business View Inner Join Relationship</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Business View Inner Join Relationship</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBusinessViewInnerJoinRelationship(BusinessViewInnerJoinRelationship object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Simple Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Simple Business Column</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSimpleBusinessColumn(SimpleBusinessColumn object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Calculated Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Calculated Business Column</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCalculatedBusinessColumn(CalculatedBusinessColumn object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseModelObject(ModelObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //BusinessModelSwitch
