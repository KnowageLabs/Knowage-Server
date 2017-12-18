/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.analytical.impl;

import it.eng.spagobi.meta.model.analytical.*;
import it.eng.spagobi.meta.model.analytical.AnalyticalModel;
import it.eng.spagobi.meta.model.analytical.AnalyticalModelFactory;
import it.eng.spagobi.meta.model.analytical.AnalyticalModelPackage;

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
public class AnalyticalModelFactoryImpl extends EFactoryImpl implements AnalyticalModelFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static AnalyticalModelFactory init() {
		try {
			AnalyticalModelFactory theAnalyticalModelFactory = (AnalyticalModelFactory)EPackage.Registry.INSTANCE.getEFactory("http:///it/eng/spagobi/meta/model/analytical.ecore"); 
			if (theAnalyticalModelFactory != null) {
				return theAnalyticalModelFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new AnalyticalModelFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyticalModelFactoryImpl() {
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
			case AnalyticalModelPackage.ANALYTICAL_MODEL: return createAnalyticalModel();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyticalModel createAnalyticalModel() {
		AnalyticalModelImpl analyticalModel = new AnalyticalModelImpl();
		return analyticalModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyticalModelPackage getAnalyticalModelPackage() {
		return (AnalyticalModelPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static AnalyticalModelPackage getPackage() {
		return AnalyticalModelPackage.eINSTANCE;
	}

} //AnalyticalModelFactoryImpl
