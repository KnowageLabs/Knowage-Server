/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.physical;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage
 * @generated
 */
public interface PhysicalModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	PhysicalModelFactory eINSTANCE = it.eng.spagobi.meta.model.physical.impl.PhysicalModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Physical Column</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Physical Column</em>'.
	 * @generated
	 */
	PhysicalColumn createPhysicalColumn();

	/**
	 * Returns a new object of class '<em>Physical Foreign Key</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Physical Foreign Key</em>'.
	 * @generated
	 */
	PhysicalForeignKey createPhysicalForeignKey();

	/**
	 * Returns a new object of class '<em>Physical Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Physical Model</em>'.
	 * @generated
	 */
	PhysicalModel createPhysicalModel();

	/**
	 * Returns a new object of class '<em>Physical Primary Key</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Physical Primary Key</em>'.
	 * @generated
	 */
	PhysicalPrimaryKey createPhysicalPrimaryKey();

	/**
	 * Returns a new object of class '<em>Physical Table</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Physical Table</em>'.
	 * @generated
	 */
	PhysicalTable createPhysicalTable();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	PhysicalModelPackage getPhysicalModelPackage();

} //PhysicalModelFactory
