/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage
 * @generated
 */
public interface BusinessModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BusinessModelFactory eINSTANCE = it.eng.spagobi.meta.model.business.impl.BusinessModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Business Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Model</em>'.
	 * @generated
	 */
	BusinessModel createBusinessModel();

	/**
	 * Returns a new object of class '<em>Business Table</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Table</em>'.
	 * @generated
	 */
	BusinessTable createBusinessTable();

	/**
	 * Returns a new object of class '<em>Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Column</em>'.
	 * @generated
	 */
	BusinessColumn createBusinessColumn();

	/**
	 * Returns a new object of class '<em>Business Column Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Column Set</em>'.
	 * @generated
	 */
	BusinessColumnSet createBusinessColumnSet();

	/**
	 * Returns a new object of class '<em>Business Relationship</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Relationship</em>'.
	 * @generated
	 */
	BusinessRelationship createBusinessRelationship();

	/**
	 * Returns a new object of class '<em>Business View</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business View</em>'.
	 * @generated
	 */
	BusinessView createBusinessView();

	/**
	 * Returns a new object of class '<em>Business Domain</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Domain</em>'.
	 * @generated
	 */
	BusinessDomain createBusinessDomain();

	/**
	 * Returns a new object of class '<em>Business Identifier</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business Identifier</em>'.
	 * @generated
	 */
	BusinessIdentifier createBusinessIdentifier();

	/**
	 * Returns a new object of class '<em>Business View Inner Join Relationship</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Business View Inner Join Relationship</em>'.
	 * @generated
	 */
	BusinessViewInnerJoinRelationship createBusinessViewInnerJoinRelationship();

	/**
	 * Returns a new object of class '<em>Simple Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Simple Business Column</em>'.
	 * @generated
	 */
	SimpleBusinessColumn createSimpleBusinessColumn();

	/**
	 * Returns a new object of class '<em>Calculated Business Column</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Calculated Business Column</em>'.
	 * @generated
	 */
	CalculatedBusinessColumn createCalculatedBusinessColumn();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	BusinessModelPackage getBusinessModelPackage();

} //BusinessModelFactory
