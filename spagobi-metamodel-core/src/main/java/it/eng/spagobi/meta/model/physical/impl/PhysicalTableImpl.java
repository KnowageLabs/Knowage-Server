/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.physical.impl;

import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalForeignKey;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalModelPackage;
import it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Physical Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalTableImpl#getComment <em>Comment</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalTableImpl#getType <em>Type</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalTableImpl#getModel <em>Model</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalTableImpl#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class PhysicalTableImpl extends ModelObjectImpl implements PhysicalTable {
	/**
	 * The default value of the '{@link #getComment() <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComment() <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected String comment = COMMENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getColumns() <em>Columns</em>}' containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<PhysicalColumn> columns;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PhysicalTableImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PhysicalModelPackage.Literals.PHYSICAL_TABLE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getComment() {
		return comment;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setComment(String newComment) {
		String oldComment = comment;
		comment = newComment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_TABLE__COMMENT, oldComment, comment));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setType(String newType) {
		String oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_TABLE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public PhysicalModel getModel() {
		if (eContainerFeatureID() != PhysicalModelPackage.PHYSICAL_TABLE__MODEL)
			return null;
		return (PhysicalModel) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetModel(PhysicalModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newModel, PhysicalModelPackage.PHYSICAL_TABLE__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setModel(PhysicalModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != PhysicalModelPackage.PHYSICAL_TABLE__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject) newModel).eInverseAdd(this, PhysicalModelPackage.PHYSICAL_MODEL__TABLES, PhysicalModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_TABLE__MODEL, newModel, newModel));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<PhysicalColumn> getColumns() {
		if (columns == null) {
			columns = new EObjectContainmentWithInverseEList<PhysicalColumn>(PhysicalColumn.class, this, PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS,
					PhysicalModelPackage.PHYSICAL_COLUMN__TABLE);
		}
		return columns;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			return basicSetModel((PhysicalModel) otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getColumns()).basicAdd(otherEnd, msgs);
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
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			return basicSetModel(null, msgs);
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			return ((InternalEList<?>) getColumns()).basicRemove(otherEnd, msgs);
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
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			return eInternalContainer().eInverseRemove(this, PhysicalModelPackage.PHYSICAL_MODEL__TABLES, PhysicalModel.class, msgs);
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
		case PhysicalModelPackage.PHYSICAL_TABLE__COMMENT:
			return getComment();
		case PhysicalModelPackage.PHYSICAL_TABLE__TYPE:
			return getType();
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			return getModel();
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			return getColumns();
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
		case PhysicalModelPackage.PHYSICAL_TABLE__COMMENT:
			setComment((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__TYPE:
			setType((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			setModel((PhysicalModel) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			getColumns().clear();
			getColumns().addAll((Collection<? extends PhysicalColumn>) newValue);
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
		case PhysicalModelPackage.PHYSICAL_TABLE__COMMENT:
			setComment(COMMENT_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__TYPE:
			setType(TYPE_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			setModel((PhysicalModel) null);
			return;
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			getColumns().clear();
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
		case PhysicalModelPackage.PHYSICAL_TABLE__COMMENT:
			return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
		case PhysicalModelPackage.PHYSICAL_TABLE__TYPE:
			return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
		case PhysicalModelPackage.PHYSICAL_TABLE__MODEL:
			return getModel() != null;
		case PhysicalModelPackage.PHYSICAL_TABLE__COLUMNS:
			return columns != null && !columns.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (comment: ");
		result.append(comment);
		result.append(", type: ");
		result.append(type);
		result.append(')');
		return result.toString();
	}

	// =========================================================================
	// Utility methods
	// =========================================================================

	@Override
	public PhysicalPrimaryKey getPrimaryKey() {
		return (getModel() != null) ? getModel().getPrimaryKey(this) : null;
	}

	@Override
	public PhysicalColumn getColumn(String name) {
		PhysicalColumn column;
		Iterator<PhysicalColumn> it = getColumns().iterator();
		while (it.hasNext()) {
			column = it.next();
			if (name.equalsIgnoreCase(column.getName())) {
				return column;
			}
		}
		return null;
	}

	@Override
	public List<PhysicalForeignKey> getForeignKeys() {
		return getModel().getForeignKeys(this);
	}

	@Override
	public List<PhysicalForeignKey> getForeignKeysInvolvingTable() {
		return getModel().getForeignKeysInvolvingTable(this);
	}

	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getModel().getParentModel().getPropertyTypes();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhysicalTableImpl other = (PhysicalTableImpl) obj;
		if (getModel() == null) {
			if (other.getModel() != null)
				return false;
		} else if (!getModel().equals(other.getModel()))
			return false;
		return true;
	}

	@Override
	public boolean containsAllNotDeleted(List<PhysicalColumn> physicalColumnsToCheck) {
		List<PhysicalColumn> columns = this.getColumns();
		for (PhysicalColumn physicalColumnToCheck : physicalColumnsToCheck) {
			boolean found = false;
			for (PhysicalColumn column : columns) {
				// first check column name
				if (column.getName().equals(physicalColumnToCheck.getName())) {
					// check that the column is not market as deleted
					if (column.getProperties().get("structural.deleted") != null) {
						String isDeleted = column.getProperties().get("structural.deleted").getValue();
						if (isDeleted.equalsIgnoreCase("false")) {
							found = true;
						}
					} else {
						// for retrocompatibility before 5.2
						found = true;
					}

				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

} // PhysicalTableImpl
