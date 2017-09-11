/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Simple Business Column</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.SimpleBusinessColumnImpl#getPhysicalColumn <em>Physical Column</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SimpleBusinessColumnImpl extends BusinessColumnImpl implements SimpleBusinessColumn {
	/**
	 * The cached value of the '{@link #getPhysicalColumn() <em>Physical Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPhysicalColumn()
	 * @generated
	 * @ordered
	 */
	protected PhysicalColumn physicalColumn;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SimpleBusinessColumnImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.SIMPLE_BUSINESS_COLUMN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PhysicalColumn getPhysicalColumn() {
		if (physicalColumn != null && physicalColumn.eIsProxy()) {
			InternalEObject oldPhysicalColumn = (InternalEObject)physicalColumn;
			physicalColumn = (PhysicalColumn)eResolveProxy(oldPhysicalColumn);
			if (physicalColumn != oldPhysicalColumn) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN, oldPhysicalColumn, physicalColumn));
			}
		}
		return physicalColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PhysicalColumn basicGetPhysicalColumn() {
		return physicalColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPhysicalColumn(PhysicalColumn newPhysicalColumn) {
		PhysicalColumn oldPhysicalColumn = physicalColumn;
		physicalColumn = newPhysicalColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN, oldPhysicalColumn, physicalColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN:
				if (resolve) return getPhysicalColumn();
				return basicGetPhysicalColumn();
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
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN:
				setPhysicalColumn((PhysicalColumn)newValue);
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
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN:
				setPhysicalColumn((PhysicalColumn)null);
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
			case BusinessModelPackage.SIMPLE_BUSINESS_COLUMN__PHYSICAL_COLUMN:
				return physicalColumn != null;
		}
		return super.eIsSet(featureID);
	}

} //SimpleBusinessColumnImpl
