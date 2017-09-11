/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.impl;

import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;

import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;
import it.eng.spagobi.meta.model.olap.VirtualCube;
import it.eng.spagobi.meta.model.olap.VirtualCubeDimension;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Virtual Cube Dimension</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl#getVirtualCube <em>Virtual Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.VirtualCubeDimensionImpl#getDimension <em>Dimension</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class VirtualCubeDimensionImpl extends ModelObjectImpl implements VirtualCubeDimension {
	/**
	 * The cached value of the '{@link #getVirtualCube() <em>Virtual Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVirtualCube()
	 * @generated
	 * @ordered
	 */
	protected VirtualCube virtualCube;

	/**
	 * The cached value of the '{@link #getCube() <em>Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCube()
	 * @generated
	 * @ordered
	 */
	protected Cube cube;

	/**
	 * The cached value of the '{@link #getDimension() <em>Dimension</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimension()
	 * @generated
	 * @ordered
	 */
	protected Dimension dimension;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VirtualCubeDimensionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.VIRTUAL_CUBE_DIMENSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VirtualCube getVirtualCube() {
		if (virtualCube != null && virtualCube.eIsProxy()) {
			InternalEObject oldVirtualCube = (InternalEObject)virtualCube;
			virtualCube = (VirtualCube)eResolveProxy(oldVirtualCube);
			if (virtualCube != oldVirtualCube) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE, oldVirtualCube, virtualCube));
			}
		}
		return virtualCube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VirtualCube basicGetVirtualCube() {
		return virtualCube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetVirtualCube(VirtualCube newVirtualCube, NotificationChain msgs) {
		VirtualCube oldVirtualCube = virtualCube;
		virtualCube = newVirtualCube;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE, oldVirtualCube, newVirtualCube);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVirtualCube(VirtualCube newVirtualCube) {
		if (newVirtualCube != virtualCube) {
			NotificationChain msgs = null;
			if (virtualCube != null)
				msgs = ((InternalEObject)virtualCube).eInverseRemove(this, OlapModelPackage.VIRTUAL_CUBE__DIMENSIONS, VirtualCube.class, msgs);
			if (newVirtualCube != null)
				msgs = ((InternalEObject)newVirtualCube).eInverseAdd(this, OlapModelPackage.VIRTUAL_CUBE__DIMENSIONS, VirtualCube.class, msgs);
			msgs = basicSetVirtualCube(newVirtualCube, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE, newVirtualCube, newVirtualCube));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cube getCube() {
		if (cube != null && cube.eIsProxy()) {
			InternalEObject oldCube = (InternalEObject)cube;
			cube = (Cube)eResolveProxy(oldCube);
			if (cube != oldCube) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE, oldCube, cube));
			}
		}
		return cube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cube basicGetCube() {
		return cube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCube(Cube newCube) {
		Cube oldCube = cube;
		cube = newCube;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE, oldCube, cube));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Dimension getDimension() {
		if (dimension != null && dimension.eIsProxy()) {
			InternalEObject oldDimension = (InternalEObject)dimension;
			dimension = (Dimension)eResolveProxy(oldDimension);
			if (dimension != oldDimension) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION, oldDimension, dimension));
			}
		}
		return dimension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Dimension basicGetDimension() {
		return dimension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDimension(Dimension newDimension) {
		Dimension oldDimension = dimension;
		dimension = newDimension;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION, oldDimension, dimension));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				if (virtualCube != null)
					msgs = ((InternalEObject)virtualCube).eInverseRemove(this, OlapModelPackage.VIRTUAL_CUBE__DIMENSIONS, VirtualCube.class, msgs);
				return basicSetVirtualCube((VirtualCube)otherEnd, msgs);
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
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				return basicSetVirtualCube(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				if (resolve) return getVirtualCube();
				return basicGetVirtualCube();
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE:
				if (resolve) return getCube();
				return basicGetCube();
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION:
				if (resolve) return getDimension();
				return basicGetDimension();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				setVirtualCube((VirtualCube)newValue);
				return;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE:
				setCube((Cube)newValue);
				return;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION:
				setDimension((Dimension)newValue);
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
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				setVirtualCube((VirtualCube)null);
				return;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE:
				setCube((Cube)null);
				return;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION:
				setDimension((Dimension)null);
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
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__VIRTUAL_CUBE:
				return virtualCube != null;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__CUBE:
				return cube != null;
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION__DIMENSION:
				return dimension != null;
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getCube().getModel().getParentModel().getPropertyTypes();
	}

} //VirtualCubeDimensionImpl
