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

import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModelPackage;
import it.eng.knowage.meta.model.impl.ModelObjectImpl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Business Column</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.business.impl.BusinessColumnImpl#getTable <em>Table</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class BusinessColumnImpl extends ModelObjectImpl implements BusinessColumn {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected BusinessColumnImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.BUSINESS_COLUMN;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public BusinessColumnSet getTable() {
		if (eContainerFeatureID() != BusinessModelPackage.BUSINESS_COLUMN__TABLE)
			return null;
		return (BusinessColumnSet) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetTable(BusinessColumnSet newTable, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newTable, BusinessModelPackage.BUSINESS_COLUMN__TABLE, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setTable(BusinessColumnSet newTable) {
		if (newTable != eInternalContainer() || (eContainerFeatureID() != BusinessModelPackage.BUSINESS_COLUMN__TABLE && newTable != null)) {
			if (EcoreUtil.isAncestor(this, newTable))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newTable != null)
				msgs = ((InternalEObject) newTable).eInverseAdd(this, BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS, BusinessColumnSet.class, msgs);
			msgs = basicSetTable(newTable, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_COLUMN__TABLE, newTable, newTable));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			return basicSetTable((BusinessColumnSet) otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			return basicSetTable(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			return eInternalContainer().eInverseRemove(this, BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS, BusinessColumnSet.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			return getTable();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			setTable((BusinessColumnSet) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			setTable((BusinessColumnSet) null);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_COLUMN__TABLE:
			return getTable() != null;
		}
		return super.eIsSet(featureID);
	}

	// =========================================================================
	// Utility methods
	// =========================================================================

	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getTable().getModel().getParentModel().getPropertyTypes();
	}

	@Override
	public boolean isIdentifier() {

		BusinessIdentifier identifier;

		identifier = getTable().getIdentifier();

		if (identifier == null)
			return false;

		for (BusinessColumn column : identifier.getColumns()) {
			if (this.equals(column)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isFilteredByProfileAttribute() {
		return isPropertyFilled("structural.attribute");
	}

	@Override
	public boolean isFilteredByRoleVisibility() {
		return isPropertyFilled("behavioural.notEnabledRoles");
	}

	private boolean isPropertyFilled(String propertyName) {
		boolean filled = false;
		if (this.getProperties().get(propertyName) != null) {
			ModelProperty modelProperty = this.getProperties().get(propertyName);
			String propertyValue = modelProperty.getValue();
			if (!propertyValue.equals("")) {
				filled = true;
			}
		}
		return filled;
	}

	@Override
	public boolean isPartOfCompositeIdentifier() {
		if (isIdentifier() == false)
			return false;
		return (getTable().getIdentifier().getColumns().size() > 1);
	}

} // BusinessColumnImpl
