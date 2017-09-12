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
package it.eng.knowage.meta.model.olap.impl;

import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.impl.ModelObjectImpl;
import it.eng.knowage.meta.model.olap.Cube;
import it.eng.knowage.meta.model.olap.Dimension;
import it.eng.knowage.meta.model.olap.Hierarchy;
import it.eng.knowage.meta.model.olap.Measure;
import it.eng.knowage.meta.model.olap.OlapModel;
import it.eng.knowage.meta.model.olap.OlapModelPackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dimension</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.olap.impl.DimensionImpl#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.impl.DimensionImpl#getHierarchies <em>Hierarchies</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.impl.DimensionImpl#getModel <em>Model</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DimensionImpl extends ModelObjectImpl implements Dimension {
	/**
	 * The cached value of the '{@link #getTable() <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTable()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumnSet table;

	/**
	 * The cached value of the '{@link #getHierarchies() <em>Hierarchies</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHierarchies()
	 * @generated
	 * @ordered
	 */
	protected EList<Hierarchy> hierarchies;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DimensionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.DIMENSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet getTable() {
		if (table != null && table.eIsProxy()) {
			InternalEObject oldTable = (InternalEObject)table;
			table = (BusinessColumnSet)eResolveProxy(oldTable);
			if (table != oldTable) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.DIMENSION__TABLE, oldTable, table));
			}
		}
		return table;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet basicGetTable() {
		return table;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTable(BusinessColumnSet newTable) {
		BusinessColumnSet oldTable = table;
		table = newTable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.DIMENSION__TABLE, oldTable, table));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Hierarchy> getHierarchies() {
		if (hierarchies == null) {
			hierarchies = new EObjectContainmentWithInverseEList<Hierarchy>(Hierarchy.class, this, OlapModelPackage.DIMENSION__HIERARCHIES, OlapModelPackage.HIERARCHY__DIMENSION);
		}
		return hierarchies;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModel getModel() {
		if (eContainerFeatureID() != OlapModelPackage.DIMENSION__MODEL) return null;
		return (OlapModel)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetModel(OlapModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newModel, OlapModelPackage.DIMENSION__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setModel(OlapModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != OlapModelPackage.DIMENSION__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject)newModel).eInverseAdd(this, OlapModelPackage.OLAP_MODEL__DIMENSIONS, OlapModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.DIMENSION__MODEL, newModel, newModel));
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
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getHierarchies()).basicAdd(otherEnd, msgs);
			case OlapModelPackage.DIMENSION__MODEL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetModel((OlapModel)otherEnd, msgs);
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
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				return ((InternalEList<?>)getHierarchies()).basicRemove(otherEnd, msgs);
			case OlapModelPackage.DIMENSION__MODEL:
				return basicSetModel(null, msgs);
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
			case OlapModelPackage.DIMENSION__MODEL:
				return eInternalContainer().eInverseRemove(this, OlapModelPackage.OLAP_MODEL__DIMENSIONS, OlapModel.class, msgs);
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
			case OlapModelPackage.DIMENSION__TABLE:
				if (resolve) return getTable();
				return basicGetTable();
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				return getHierarchies();
			case OlapModelPackage.DIMENSION__MODEL:
				return getModel();
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
			case OlapModelPackage.DIMENSION__TABLE:
				setTable((BusinessColumnSet)newValue);
				return;
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				getHierarchies().clear();
				getHierarchies().addAll((Collection<? extends Hierarchy>)newValue);
				return;
			case OlapModelPackage.DIMENSION__MODEL:
				setModel((OlapModel)newValue);
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
			case OlapModelPackage.DIMENSION__TABLE:
				setTable((BusinessColumnSet)null);
				return;
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				getHierarchies().clear();
				return;
			case OlapModelPackage.DIMENSION__MODEL:
				setModel((OlapModel)null);
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
			case OlapModelPackage.DIMENSION__TABLE:
				return table != null;
			case OlapModelPackage.DIMENSION__HIERARCHIES:
				return hierarchies != null && !hierarchies.isEmpty();
			case OlapModelPackage.DIMENSION__MODEL:
				return getModel() != null;
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.knowage.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getModel().getParentModel().getPropertyTypes();
	}
	


} //DimensionImpl
