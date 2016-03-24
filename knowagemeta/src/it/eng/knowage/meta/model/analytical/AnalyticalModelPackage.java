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
package it.eng.knowage.meta.model.analytical;

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
 * @see it.eng.knowage.meta.model.analytical.AnalyticalModelFactory
 * @model kind="package"
 * @generated
 */
public interface AnalyticalModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "analytical";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///it/eng/spagobi/meta/model/analytical.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "it.eng.knowage.meta.model.analytical";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	AnalyticalModelPackage eINSTANCE = it.eng.knowage.meta.model.analytical.impl.AnalyticalModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link it.eng.knowage.meta.model.analytical.impl.AnalyticalModelImpl <em>Analytical Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see it.eng.knowage.meta.model.analytical.impl.AnalyticalModelImpl
	 * @see it.eng.knowage.meta.model.analytical.impl.AnalyticalModelPackageImpl#getAnalyticalModel()
	 * @generated
	 */
	int ANALYTICAL_MODEL = 0;

	/**
	 * The number of structural features of the '<em>Analytical Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYTICAL_MODEL_FEATURE_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link it.eng.knowage.meta.model.analytical.AnalyticalModel <em>Analytical Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Analytical Model</em>'.
	 * @see it.eng.knowage.meta.model.analytical.AnalyticalModel
	 * @generated
	 */
	EClass getAnalyticalModel();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	AnalyticalModelFactory getAnalyticalModelFactory();

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
		 * The meta object literal for the '{@link it.eng.knowage.meta.model.analytical.impl.AnalyticalModelImpl <em>Analytical Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see it.eng.knowage.meta.model.analytical.impl.AnalyticalModelImpl
		 * @see it.eng.knowage.meta.model.analytical.impl.AnalyticalModelPackageImpl#getAnalyticalModel()
		 * @generated
		 */
		EClass ANALYTICAL_MODEL = eINSTANCE.getAnalyticalModel();

	}

} //AnalyticalModelPackage
