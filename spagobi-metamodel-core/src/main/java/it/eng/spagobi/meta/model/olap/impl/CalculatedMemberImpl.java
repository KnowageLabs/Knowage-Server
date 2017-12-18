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

import it.eng.spagobi.meta.model.olap.CalculatedMember;
import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Hierarchy;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Calculated Member</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CalculatedMemberImpl#getHierarchy <em>Hierarchy</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CalculatedMemberImpl extends ModelObjectImpl implements CalculatedMember {
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
	 * The cached value of the '{@link #getHierarchy() <em>Hierarchy</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHierarchy()
	 * @generated
	 * @ordered
	 */
	protected Hierarchy hierarchy;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CalculatedMemberImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.CALCULATED_MEMBER;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.CALCULATED_MEMBER__CUBE, oldCube, cube));
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
	public NotificationChain basicSetCube(Cube newCube, NotificationChain msgs) {
		Cube oldCube = cube;
		cube = newCube;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, OlapModelPackage.CALCULATED_MEMBER__CUBE, oldCube, newCube);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCube(Cube newCube) {
		if (newCube != cube) {
			NotificationChain msgs = null;
			if (cube != null)
				msgs = ((InternalEObject)cube).eInverseRemove(this, OlapModelPackage.CUBE__CALCULATED_MEMBERS, Cube.class, msgs);
			if (newCube != null)
				msgs = ((InternalEObject)newCube).eInverseAdd(this, OlapModelPackage.CUBE__CALCULATED_MEMBERS, Cube.class, msgs);
			msgs = basicSetCube(newCube, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.CALCULATED_MEMBER__CUBE, newCube, newCube));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Hierarchy getHierarchy() {
		if (hierarchy != null && hierarchy.eIsProxy()) {
			InternalEObject oldHierarchy = (InternalEObject)hierarchy;
			hierarchy = (Hierarchy)eResolveProxy(oldHierarchy);
			if (hierarchy != oldHierarchy) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.CALCULATED_MEMBER__HIERARCHY, oldHierarchy, hierarchy));
			}
		}
		return hierarchy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Hierarchy basicGetHierarchy() {
		return hierarchy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHierarchy(Hierarchy newHierarchy) {
		Hierarchy oldHierarchy = hierarchy;
		hierarchy = newHierarchy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.CALCULATED_MEMBER__HIERARCHY, oldHierarchy, hierarchy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				if (cube != null)
					msgs = ((InternalEObject)cube).eInverseRemove(this, OlapModelPackage.CUBE__CALCULATED_MEMBERS, Cube.class, msgs);
				return basicSetCube((Cube)otherEnd, msgs);
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
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				return basicSetCube(null, msgs);
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
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				if (resolve) return getCube();
				return basicGetCube();
			case OlapModelPackage.CALCULATED_MEMBER__HIERARCHY:
				if (resolve) return getHierarchy();
				return basicGetHierarchy();
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
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				setCube((Cube)newValue);
				return;
			case OlapModelPackage.CALCULATED_MEMBER__HIERARCHY:
				setHierarchy((Hierarchy)newValue);
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
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				setCube((Cube)null);
				return;
			case OlapModelPackage.CALCULATED_MEMBER__HIERARCHY:
				setHierarchy((Hierarchy)null);
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
			case OlapModelPackage.CALCULATED_MEMBER__CUBE:
				return cube != null;
			case OlapModelPackage.CALCULATED_MEMBER__HIERARCHY:
				return hierarchy != null;
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

} //CalculatedMemberImpl
