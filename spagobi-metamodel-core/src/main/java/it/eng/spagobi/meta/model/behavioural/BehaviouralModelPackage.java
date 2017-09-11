/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.behavioural;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

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
 * @see it.eng.spagobi.meta.model.behavioural.BehaviouralModelFactory
 * @model kind="package"
 * @generated
 */
public interface BehaviouralModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "behavioural";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///it/eng/spagobi/meta/model/behavioural.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "it.eng.spagobi.meta.model.behavioural";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BehaviouralModelPackage eINSTANCE = it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelImpl <em>Behavioural Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelImpl
	 * @see it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelPackageImpl#getBehaviouralModel()
	 * @generated
	 */
	int BEHAVIOURAL_MODEL = 0;

	/**
	 * The number of structural features of the '<em>Behavioural Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEHAVIOURAL_MODEL_FEATURE_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link it.eng.spagobi.meta.model.behavioural.BehaviouralModel <em>Behavioural Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Behavioural Model</em>'.
	 * @see it.eng.spagobi.meta.model.behavioural.BehaviouralModel
	 * @generated
	 */
	EClass getBehaviouralModel();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	BehaviouralModelFactory getBehaviouralModelFactory();

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
		 * The meta object literal for the '{@link it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelImpl <em>Behavioural Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelImpl
		 * @see it.eng.spagobi.meta.model.behavioural.impl.BehaviouralModelPackageImpl#getBehaviouralModel()
		 * @generated
		 */
		EClass BEHAVIOURAL_MODEL = eINSTANCE.getBehaviouralModel();

	}

} //BehaviouralModelPackage
