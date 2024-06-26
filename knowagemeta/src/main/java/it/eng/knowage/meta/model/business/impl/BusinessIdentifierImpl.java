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

import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelPackage;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.impl.ModelObjectImpl;
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Business Identifier</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.business.impl.BusinessIdentifierImpl#getModel <em>Model</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.business.impl.BusinessIdentifierImpl#getTable <em>Table</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.business.impl.BusinessIdentifierImpl#getColumns <em>Columns</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.business.impl.BusinessIdentifierImpl#getPhysicalPrimaryKey <em>Physical Primary Key</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class BusinessIdentifierImpl extends ModelObjectImpl implements BusinessIdentifier {
	/**
	 * The cached value of the '{@link #getTable() <em>Table</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTable()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumnSet table;

	/**
	 * The cached value of the '{@link #getColumns() <em>Columns</em>}' reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessColumn> columns;

	/**
	 * The cached value of the '{@link #getPhysicalPrimaryKey() <em>Physical Primary Key</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getPhysicalPrimaryKey()
	 * @generated
	 * @ordered
	 */
	protected PhysicalPrimaryKey physicalPrimaryKey;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected BusinessIdentifierImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.BUSINESS_IDENTIFIER;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	@JsonIgnore
	public BusinessModel getModel() {
		if (eContainerFeatureID() != BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL)
			return null;
		return (BusinessModel) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetModel(BusinessModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newModel, BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setModel(BusinessModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject) newModel).eInverseAdd(this, BusinessModelPackage.BUSINESS_MODEL__IDENTIFIERS, BusinessModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL, newModel, newModel));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public BusinessColumnSet getTable() {
		if (table != null && table.eIsProxy()) {
			InternalEObject oldTable = (InternalEObject) table;
			table = (BusinessColumnSet) eResolveProxy(oldTable);
			if (table != oldTable) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE, oldTable, table));
			}
		}
		return table;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public BusinessColumnSet basicGetTable() {
		return table;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setTable(BusinessColumnSet newTable) {
		BusinessColumnSet oldTable = table;
		table = newTable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE, oldTable, table));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<BusinessColumn> getColumns() {
		if (columns == null) {
			columns = new EObjectResolvingEList<BusinessColumn>(BusinessColumn.class, this, BusinessModelPackage.BUSINESS_IDENTIFIER__COLUMNS);
		}
		return columns;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public PhysicalPrimaryKey getPhysicalPrimaryKey() {
		if (physicalPrimaryKey != null && physicalPrimaryKey.eIsProxy()) {
			InternalEObject oldPhysicalPrimaryKey = (InternalEObject) physicalPrimaryKey;
			physicalPrimaryKey = (PhysicalPrimaryKey) eResolveProxy(oldPhysicalPrimaryKey);
			if (physicalPrimaryKey != oldPhysicalPrimaryKey) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY,
							oldPhysicalPrimaryKey, physicalPrimaryKey));
			}
		}
		return physicalPrimaryKey;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public PhysicalPrimaryKey basicGetPhysicalPrimaryKey() {
		return physicalPrimaryKey;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setPhysicalPrimaryKey(PhysicalPrimaryKey newPhysicalPrimaryKey) {
		PhysicalPrimaryKey oldPhysicalPrimaryKey = physicalPrimaryKey;
		physicalPrimaryKey = newPhysicalPrimaryKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY, oldPhysicalPrimaryKey,
					physicalPrimaryKey));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			return basicSetModel((BusinessModel) otherEnd, msgs);
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
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			return basicSetModel(null, msgs);
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
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			return eInternalContainer().eInverseRemove(this, BusinessModelPackage.BUSINESS_MODEL__IDENTIFIERS, BusinessModel.class, msgs);
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
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			return getModel();
		case BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE:
			if (resolve)
				return getTable();
			return basicGetTable();
		case BusinessModelPackage.BUSINESS_IDENTIFIER__COLUMNS:
			return getColumns();
		case BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY:
			if (resolve)
				return getPhysicalPrimaryKey();
			return basicGetPhysicalPrimaryKey();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			setModel((BusinessModel) newValue);
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE:
			setTable((BusinessColumnSet) newValue);
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__COLUMNS:
			getColumns().clear();
			getColumns().addAll((Collection<? extends BusinessColumn>) newValue);
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY:
			setPhysicalPrimaryKey((PhysicalPrimaryKey) newValue);
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
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			setModel((BusinessModel) null);
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE:
			setTable((BusinessColumnSet) null);
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__COLUMNS:
			getColumns().clear();
			return;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY:
			setPhysicalPrimaryKey((PhysicalPrimaryKey) null);
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
		case BusinessModelPackage.BUSINESS_IDENTIFIER__MODEL:
			return getModel() != null;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__TABLE:
			return table != null;
		case BusinessModelPackage.BUSINESS_IDENTIFIER__COLUMNS:
			return columns != null && !columns.isEmpty();
		case BusinessModelPackage.BUSINESS_IDENTIFIER__PHYSICAL_PRIMARY_KEY:
			return physicalPrimaryKey != null;
		}
		return super.eIsSet(featureID);
	}

	// =========================================================================
	// Utility methods
	// =========================================================================

	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getModel().getParentModel().getPropertyTypes();
	}

	@Override
	public List<SimpleBusinessColumn> getSimpleBusinessColumns() {
		EList<BusinessColumn> businessColumns = getColumns();
		List<SimpleBusinessColumn> simpleColumns = new ArrayList<SimpleBusinessColumn>();

		for (BusinessColumn column : businessColumns) {
			if (column instanceof SimpleBusinessColumn) {
				simpleColumns.add((SimpleBusinessColumn) column);
			}
		}
		return simpleColumns;
	}

} // BusinessIdentifierImpl
