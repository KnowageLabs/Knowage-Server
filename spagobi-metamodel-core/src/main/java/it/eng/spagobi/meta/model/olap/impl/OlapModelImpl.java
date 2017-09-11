/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.impl;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;
import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;

import it.eng.spagobi.meta.model.olap.VirtualCube;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Olap Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl#getParentModel <em>Parent Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl#getCubes <em>Cubes</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl#getVirtualCubes <em>Virtual Cubes</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.OlapModelImpl#getDimensions <em>Dimensions</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OlapModelImpl extends ModelObjectImpl implements OlapModel {
	/**
	 * The cached value of the '{@link #getCubes() <em>Cubes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCubes()
	 * @generated
	 * @ordered
	 */
	protected EList<Cube> cubes;
	/**
	 * The cached value of the '{@link #getVirtualCubes() <em>Virtual Cubes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVirtualCubes()
	 * @generated
	 * @ordered
	 */
	protected EList<VirtualCube> virtualCubes;
	/**
	 * The cached value of the '{@link #getDimensions() <em>Dimensions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimensions()
	 * @generated
	 * @ordered
	 */
	protected EList<Dimension> dimensions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OlapModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.OLAP_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Model getParentModel() {
		if (eContainerFeatureID() != OlapModelPackage.OLAP_MODEL__PARENT_MODEL) return null;
		return (Model)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParentModel(Model newParentModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParentModel, OlapModelPackage.OLAP_MODEL__PARENT_MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentModel(Model newParentModel) {
		if (newParentModel != eInternalContainer() || (eContainerFeatureID() != OlapModelPackage.OLAP_MODEL__PARENT_MODEL && newParentModel != null)) {
			if (EcoreUtil.isAncestor(this, newParentModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParentModel != null)
				msgs = ((InternalEObject)newParentModel).eInverseAdd(this, ModelPackage.MODEL__OLAP_MODELS, Model.class, msgs);
			msgs = basicSetParentModel(newParentModel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.OLAP_MODEL__PARENT_MODEL, newParentModel, newParentModel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Cube> getCubes() {
		if (cubes == null) {
			cubes = new EObjectContainmentWithInverseEList<Cube>(Cube.class, this, OlapModelPackage.OLAP_MODEL__CUBES, OlapModelPackage.CUBE__MODEL);
		}
		return cubes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<VirtualCube> getVirtualCubes() {
		if (virtualCubes == null) {
			virtualCubes = new EObjectContainmentWithInverseEList<VirtualCube>(VirtualCube.class, this, OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES, OlapModelPackage.VIRTUAL_CUBE__MODEL);
		}
		return virtualCubes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Dimension> getDimensions() {
		if (dimensions == null) {
			dimensions = new EObjectContainmentWithInverseEList<Dimension>(Dimension.class, this, OlapModelPackage.OLAP_MODEL__DIMENSIONS, OlapModelPackage.DIMENSION__MODEL);
		}
		return dimensions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParentModel((Model)otherEnd, msgs);
			case OlapModelPackage.OLAP_MODEL__CUBES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getCubes()).basicAdd(otherEnd, msgs);
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getVirtualCubes()).basicAdd(otherEnd, msgs);
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getDimensions()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				return basicSetParentModel(null, msgs);
			case OlapModelPackage.OLAP_MODEL__CUBES:
				return ((InternalEList<?>)getCubes()).basicRemove(otherEnd, msgs);
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				return ((InternalEList<?>)getVirtualCubes()).basicRemove(otherEnd, msgs);
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				return ((InternalEList<?>)getDimensions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				return eInternalContainer().eInverseRemove(this, ModelPackage.MODEL__OLAP_MODELS, Model.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				return getParentModel();
			case OlapModelPackage.OLAP_MODEL__CUBES:
				return getCubes();
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				return getVirtualCubes();
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				return getDimensions();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				setParentModel((Model)newValue);
				return;
			case OlapModelPackage.OLAP_MODEL__CUBES:
				getCubes().clear();
				getCubes().addAll((Collection<? extends Cube>)newValue);
				return;
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				getVirtualCubes().clear();
				getVirtualCubes().addAll((Collection<? extends VirtualCube>)newValue);
				return;
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				getDimensions().clear();
				getDimensions().addAll((Collection<? extends Dimension>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				setParentModel((Model)null);
				return;
			case OlapModelPackage.OLAP_MODEL__CUBES:
				getCubes().clear();
				return;
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				getVirtualCubes().clear();
				return;
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				getDimensions().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case OlapModelPackage.OLAP_MODEL__PARENT_MODEL:
				return getParentModel() != null;
			case OlapModelPackage.OLAP_MODEL__CUBES:
				return cubes != null && !cubes.isEmpty();
			case OlapModelPackage.OLAP_MODEL__VIRTUAL_CUBES:
				return virtualCubes != null && !virtualCubes.isEmpty();
			case OlapModelPackage.OLAP_MODEL__DIMENSIONS:
				return dimensions != null && !dimensions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getParentModel().getPropertyTypes();

	}

} //OlapModelImpl
