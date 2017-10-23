/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.impl;

import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessColumn;

import it.eng.spagobi.meta.model.impl.ModelObjectImpl;

import it.eng.spagobi.meta.model.olap.Hierarchy;
import it.eng.spagobi.meta.model.olap.Level;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Level</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getHierarchy <em>Hierarchy</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getColumn <em>Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getOrdinalColumn <em>Ordinal Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getNameColumn <em>Name Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getCaptionColumn <em>Caption Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.LevelImpl#getPropertyColumns <em>Property Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LevelImpl extends ModelObjectImpl implements Level {
	/**
	 * The cached value of the '{@link #getColumn() <em>Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumn column;

	/**
	 * The cached value of the '{@link #getOrdinalColumn() <em>Ordinal Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrdinalColumn()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumn ordinalColumn;

	/**
	 * The cached value of the '{@link #getNameColumn() <em>Name Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNameColumn()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumn nameColumn;

	/**
	 * The cached value of the '{@link #getCaptionColumn() <em>Caption Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCaptionColumn()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumn captionColumn;

	/**
	 * The cached value of the '{@link #getPropertyColumns() <em>Property Columns</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPropertyColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessColumn> propertyColumns;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LevelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.LEVEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Hierarchy getHierarchy() {
		if (eContainerFeatureID() != OlapModelPackage.LEVEL__HIERARCHY) return null;
		return (Hierarchy)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHierarchy(Hierarchy newHierarchy, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newHierarchy, OlapModelPackage.LEVEL__HIERARCHY, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHierarchy(Hierarchy newHierarchy) {
		if (newHierarchy != eInternalContainer() || (eContainerFeatureID() != OlapModelPackage.LEVEL__HIERARCHY && newHierarchy != null)) {
			if (EcoreUtil.isAncestor(this, newHierarchy))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newHierarchy != null)
				msgs = ((InternalEObject)newHierarchy).eInverseAdd(this, OlapModelPackage.HIERARCHY__LEVELS, Hierarchy.class, msgs);
			msgs = basicSetHierarchy(newHierarchy, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.LEVEL__HIERARCHY, newHierarchy, newHierarchy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn getColumn() {
		if (column != null && column.eIsProxy()) {
			InternalEObject oldColumn = (InternalEObject)column;
			column = (BusinessColumn)eResolveProxy(oldColumn);
			if (column != oldColumn) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.LEVEL__COLUMN, oldColumn, column));
			}
		}
		return column;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn basicGetColumn() {
		return column;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setColumn(BusinessColumn newColumn) {
		BusinessColumn oldColumn = column;
		column = newColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.LEVEL__COLUMN, oldColumn, column));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn getOrdinalColumn() {
		if (ordinalColumn != null && ordinalColumn.eIsProxy()) {
			InternalEObject oldOrdinalColumn = (InternalEObject)ordinalColumn;
			ordinalColumn = (BusinessColumn)eResolveProxy(oldOrdinalColumn);
			if (ordinalColumn != oldOrdinalColumn) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.LEVEL__ORDINAL_COLUMN, oldOrdinalColumn, ordinalColumn));
			}
		}
		return ordinalColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn basicGetOrdinalColumn() {
		return ordinalColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOrdinalColumn(BusinessColumn newOrdinalColumn) {
		BusinessColumn oldOrdinalColumn = ordinalColumn;
		ordinalColumn = newOrdinalColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.LEVEL__ORDINAL_COLUMN, oldOrdinalColumn, ordinalColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn getNameColumn() {
		if (nameColumn != null && nameColumn.eIsProxy()) {
			InternalEObject oldNameColumn = (InternalEObject)nameColumn;
			nameColumn = (BusinessColumn)eResolveProxy(oldNameColumn);
			if (nameColumn != oldNameColumn) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.LEVEL__NAME_COLUMN, oldNameColumn, nameColumn));
			}
		}
		return nameColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn basicGetNameColumn() {
		return nameColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNameColumn(BusinessColumn newNameColumn) {
		BusinessColumn oldNameColumn = nameColumn;
		nameColumn = newNameColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.LEVEL__NAME_COLUMN, oldNameColumn, nameColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn getCaptionColumn() {
		if (captionColumn != null && captionColumn.eIsProxy()) {
			InternalEObject oldCaptionColumn = (InternalEObject)captionColumn;
			captionColumn = (BusinessColumn)eResolveProxy(oldCaptionColumn);
			if (captionColumn != oldCaptionColumn) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.LEVEL__CAPTION_COLUMN, oldCaptionColumn, captionColumn));
			}
		}
		return captionColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumn basicGetCaptionColumn() {
		return captionColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCaptionColumn(BusinessColumn newCaptionColumn) {
		BusinessColumn oldCaptionColumn = captionColumn;
		captionColumn = newCaptionColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.LEVEL__CAPTION_COLUMN, oldCaptionColumn, captionColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<BusinessColumn> getPropertyColumns() {
		if (propertyColumns == null) {
			propertyColumns = new EObjectResolvingEList<BusinessColumn>(BusinessColumn.class, this, OlapModelPackage.LEVEL__PROPERTY_COLUMNS);
		}
		return propertyColumns;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OlapModelPackage.LEVEL__HIERARCHY:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetHierarchy((Hierarchy)otherEnd, msgs);
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				return basicSetHierarchy(null, msgs);
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				return eInternalContainer().eInverseRemove(this, OlapModelPackage.HIERARCHY__LEVELS, Hierarchy.class, msgs);
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				return getHierarchy();
			case OlapModelPackage.LEVEL__COLUMN:
				if (resolve) return getColumn();
				return basicGetColumn();
			case OlapModelPackage.LEVEL__ORDINAL_COLUMN:
				if (resolve) return getOrdinalColumn();
				return basicGetOrdinalColumn();
			case OlapModelPackage.LEVEL__NAME_COLUMN:
				if (resolve) return getNameColumn();
				return basicGetNameColumn();
			case OlapModelPackage.LEVEL__CAPTION_COLUMN:
				if (resolve) return getCaptionColumn();
				return basicGetCaptionColumn();
			case OlapModelPackage.LEVEL__PROPERTY_COLUMNS:
				return getPropertyColumns();
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				setHierarchy((Hierarchy)newValue);
				return;
			case OlapModelPackage.LEVEL__COLUMN:
				setColumn((BusinessColumn)newValue);
				return;
			case OlapModelPackage.LEVEL__ORDINAL_COLUMN:
				setOrdinalColumn((BusinessColumn)newValue);
				return;
			case OlapModelPackage.LEVEL__NAME_COLUMN:
				setNameColumn((BusinessColumn)newValue);
				return;
			case OlapModelPackage.LEVEL__CAPTION_COLUMN:
				setCaptionColumn((BusinessColumn)newValue);
				return;
			case OlapModelPackage.LEVEL__PROPERTY_COLUMNS:
				getPropertyColumns().clear();
				getPropertyColumns().addAll((Collection<? extends BusinessColumn>)newValue);
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				setHierarchy((Hierarchy)null);
				return;
			case OlapModelPackage.LEVEL__COLUMN:
				setColumn((BusinessColumn)null);
				return;
			case OlapModelPackage.LEVEL__ORDINAL_COLUMN:
				setOrdinalColumn((BusinessColumn)null);
				return;
			case OlapModelPackage.LEVEL__NAME_COLUMN:
				setNameColumn((BusinessColumn)null);
				return;
			case OlapModelPackage.LEVEL__CAPTION_COLUMN:
				setCaptionColumn((BusinessColumn)null);
				return;
			case OlapModelPackage.LEVEL__PROPERTY_COLUMNS:
				getPropertyColumns().clear();
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
			case OlapModelPackage.LEVEL__HIERARCHY:
				return getHierarchy() != null;
			case OlapModelPackage.LEVEL__COLUMN:
				return column != null;
			case OlapModelPackage.LEVEL__ORDINAL_COLUMN:
				return ordinalColumn != null;
			case OlapModelPackage.LEVEL__NAME_COLUMN:
				return nameColumn != null;
			case OlapModelPackage.LEVEL__CAPTION_COLUMN:
				return captionColumn != null;
			case OlapModelPackage.LEVEL__PROPERTY_COLUMNS:
				return propertyColumns != null && !propertyColumns.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getHierarchy().getDimension().getModel().getParentModel().getPropertyTypes();
	}

} //LevelImpl
