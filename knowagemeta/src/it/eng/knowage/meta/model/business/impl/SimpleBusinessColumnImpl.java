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
package it.eng.knowage.meta.model.business.impl;

import it.eng.knowage.meta.model.business.BusinessModelPackage;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;

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
 *   <li>{@link it.eng.knowage.meta.model.business.impl.SimpleBusinessColumnImpl#getPhysicalColumn <em>Physical Column</em>}</li>
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
