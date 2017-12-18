/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.util;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.olap.*;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage
 * @generated
 */
public class OlapModelAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static OlapModelPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModelAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = OlapModelPackage.eINSTANCE;
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
	protected OlapModelSwitch<Adapter> modelSwitch =
		new OlapModelSwitch<Adapter>() {
			@Override
			public Adapter caseOlapModel(OlapModel object) {
				return createOlapModelAdapter();
			}
			@Override
			public Adapter caseCube(Cube object) {
				return createCubeAdapter();
			}
			@Override
			public Adapter caseDimension(Dimension object) {
				return createDimensionAdapter();
			}
			@Override
			public Adapter caseHierarchy(Hierarchy object) {
				return createHierarchyAdapter();
			}
			@Override
			public Adapter caseLevel(Level object) {
				return createLevelAdapter();
			}
			@Override
			public Adapter caseMeasure(Measure object) {
				return createMeasureAdapter();
			}
			@Override
			public Adapter caseCalculatedMember(CalculatedMember object) {
				return createCalculatedMemberAdapter();
			}
			@Override
			public Adapter caseNamedSet(NamedSet object) {
				return createNamedSetAdapter();
			}
			@Override
			public Adapter caseVirtualCube(VirtualCube object) {
				return createVirtualCubeAdapter();
			}
			@Override
			public Adapter caseVirtualCubeDimension(VirtualCubeDimension object) {
				return createVirtualCubeDimensionAdapter();
			}
			@Override
			public Adapter caseVirtualCubeMeasure(VirtualCubeMeasure object) {
				return createVirtualCubeMeasureAdapter();
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
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.OlapModel <em>Olap Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.OlapModel
	 * @generated
	 */
	public Adapter createOlapModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.Cube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.Cube
	 * @generated
	 */
	public Adapter createCubeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.Dimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.Dimension
	 * @generated
	 */
	public Adapter createDimensionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.Hierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy
	 * @generated
	 */
	public Adapter createHierarchyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.Level <em>Level</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.Level
	 * @generated
	 */
	public Adapter createLevelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.Measure <em>Measure</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.Measure
	 * @generated
	 */
	public Adapter createMeasureAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.CalculatedMember <em>Calculated Member</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.CalculatedMember
	 * @generated
	 */
	public Adapter createCalculatedMemberAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.NamedSet <em>Named Set</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.NamedSet
	 * @generated
	 */
	public Adapter createNamedSetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.VirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube
	 * @generated
	 */
	public Adapter createVirtualCubeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension <em>Virtual Cube Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension
	 * @generated
	 */
	public Adapter createVirtualCubeDimensionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure <em>Virtual Cube Measure</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure
	 * @generated
	 */
	public Adapter createVirtualCubeMeasureAdapter() {
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

} //OlapModelAdapterFactory
