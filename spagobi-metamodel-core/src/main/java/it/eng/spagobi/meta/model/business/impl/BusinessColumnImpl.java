/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;

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
 * <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnImpl#getTable <em>Table</em>}</li>
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
