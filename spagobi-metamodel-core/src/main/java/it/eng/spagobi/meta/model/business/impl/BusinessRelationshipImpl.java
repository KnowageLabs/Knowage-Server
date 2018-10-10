/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;
import it.eng.spagobi.meta.model.physical.PhysicalForeignKey;

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

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Business Relationship</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getSourceTable <em>Source Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getDestinationTable <em>Destination Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getSourceColumns <em>Source Columns</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getDestinationColumns <em>Destination Columns</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessRelationshipImpl#getPhysicalForeignKey <em>Physical Foreign Key</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BusinessRelationshipImpl extends ModelObjectImpl implements BusinessRelationship {
	/**
	 * The cached value of the '{@link #getSourceTable() <em>Source Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceTable()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumnSet sourceTable;

	/**
	 * The cached value of the '{@link #getDestinationTable() <em>Destination Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDestinationTable()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumnSet destinationTable;

	/**
	 * The cached value of the '{@link #getSourceColumns() <em>Source Columns</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessColumn> sourceColumns;

	/**
	 * The cached value of the '{@link #getDestinationColumns() <em>Destination Columns</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDestinationColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessColumn> destinationColumns;

	/**
	 * The cached value of the '{@link #getPhysicalForeignKey() <em>Physical Foreign Key</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPhysicalForeignKey()
	 * @generated
	 * @ordered
	 */
	protected PhysicalForeignKey physicalForeignKey;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BusinessRelationshipImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.BUSINESS_RELATIONSHIP;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModel getModel() {
		if (eContainerFeatureID() != BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL) return null;
		return (BusinessModel)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetModel(BusinessModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newModel, BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setModel(BusinessModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject)newModel).eInverseAdd(this, BusinessModelPackage.BUSINESS_MODEL__RELATIONSHIPS, BusinessModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL, newModel, newModel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet getSourceTable() {
		if (sourceTable != null && sourceTable.eIsProxy()) {
			InternalEObject oldSourceTable = (InternalEObject)sourceTable;
			sourceTable = (BusinessColumnSet)eResolveProxy(oldSourceTable);
			if (sourceTable != oldSourceTable) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE, oldSourceTable, sourceTable));
			}
		}
		return sourceTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet basicGetSourceTable() {
		return sourceTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSourceTable(BusinessColumnSet newSourceTable) {
		BusinessColumnSet oldSourceTable = sourceTable;
		sourceTable = newSourceTable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE, oldSourceTable, sourceTable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet getDestinationTable() {
		if (destinationTable != null && destinationTable.eIsProxy()) {
			InternalEObject oldDestinationTable = (InternalEObject)destinationTable;
			destinationTable = (BusinessColumnSet)eResolveProxy(oldDestinationTable);
			if (destinationTable != oldDestinationTable) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE, oldDestinationTable, destinationTable));
			}
		}
		return destinationTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet basicGetDestinationTable() {
		return destinationTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDestinationTable(BusinessColumnSet newDestinationTable) {
		BusinessColumnSet oldDestinationTable = destinationTable;
		destinationTable = newDestinationTable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE, oldDestinationTable, destinationTable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<BusinessColumn> getSourceColumns() {
		if (sourceColumns == null) {
			sourceColumns = new EObjectResolvingEList<BusinessColumn>(BusinessColumn.class, this, BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_COLUMNS);
		}
		return sourceColumns;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<BusinessColumn> getDestinationColumns() {
		if (destinationColumns == null) {
			destinationColumns = new EObjectResolvingEList<BusinessColumn>(BusinessColumn.class, this, BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS);
		}
		return destinationColumns;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PhysicalForeignKey getPhysicalForeignKey() {
		if (physicalForeignKey != null && physicalForeignKey.eIsProxy()) {
			InternalEObject oldPhysicalForeignKey = (InternalEObject)physicalForeignKey;
			physicalForeignKey = (PhysicalForeignKey)eResolveProxy(oldPhysicalForeignKey);
			if (physicalForeignKey != oldPhysicalForeignKey) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY, oldPhysicalForeignKey, physicalForeignKey));
			}
		}
		return physicalForeignKey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PhysicalForeignKey basicGetPhysicalForeignKey() {
		return physicalForeignKey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPhysicalForeignKey(PhysicalForeignKey newPhysicalForeignKey) {
		PhysicalForeignKey oldPhysicalForeignKey = physicalForeignKey;
		physicalForeignKey = newPhysicalForeignKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY, oldPhysicalForeignKey, physicalForeignKey));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetModel((BusinessModel)otherEnd, msgs);
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				return eInternalContainer().eInverseRemove(this, BusinessModelPackage.BUSINESS_MODEL__RELATIONSHIPS, BusinessModel.class, msgs);
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				return getModel();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE:
				if (resolve) return getSourceTable();
				return basicGetSourceTable();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE:
				if (resolve) return getDestinationTable();
				return basicGetDestinationTable();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_COLUMNS:
				return getSourceColumns();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS:
				return getDestinationColumns();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY:
				if (resolve) return getPhysicalForeignKey();
				return basicGetPhysicalForeignKey();
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				setModel((BusinessModel)newValue);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE:
				setSourceTable((BusinessColumnSet)newValue);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE:
				setDestinationTable((BusinessColumnSet)newValue);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_COLUMNS:
				getSourceColumns().clear();
				getSourceColumns().addAll((Collection<? extends BusinessColumn>)newValue);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS:
				getDestinationColumns().clear();
				getDestinationColumns().addAll((Collection<? extends BusinessColumn>)newValue);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY:
				setPhysicalForeignKey((PhysicalForeignKey)newValue);
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				setModel((BusinessModel)null);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE:
				setSourceTable((BusinessColumnSet)null);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE:
				setDestinationTable((BusinessColumnSet)null);
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_COLUMNS:
				getSourceColumns().clear();
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS:
				getDestinationColumns().clear();
				return;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY:
				setPhysicalForeignKey((PhysicalForeignKey)null);
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
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__MODEL:
				return getModel() != null;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_TABLE:
				return sourceTable != null;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_TABLE:
				return destinationTable != null;
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__SOURCE_COLUMNS:
				return sourceColumns != null && !sourceColumns.isEmpty();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__DESTINATION_COLUMNS:
				return destinationColumns != null && !destinationColumns.isEmpty();
			case BusinessModelPackage.BUSINESS_RELATIONSHIP__PHYSICAL_FOREIGN_KEY:
				return physicalForeignKey != null;
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
	public List<SimpleBusinessColumn> getDestinationSimpleBusinessColumns() {
		EList<BusinessColumn> destinationColumns = getDestinationColumns();
		List<SimpleBusinessColumn> destinationSimpleColumns = new ArrayList<SimpleBusinessColumn>();
		for(BusinessColumn column : destinationColumns){
			if (column instanceof SimpleBusinessColumn){
				destinationSimpleColumns.add((SimpleBusinessColumn)column);
			}
		}
		return destinationSimpleColumns;
	}

	@Override
	public List<SimpleBusinessColumn> getSourceSimpleBusinessColumns() {
		EList<BusinessColumn> sourceColumns = getSourceColumns();
		List<SimpleBusinessColumn> sourceSimpleColumns = new ArrayList<SimpleBusinessColumn>();
		for(BusinessColumn column : sourceColumns){
			if (column instanceof SimpleBusinessColumn){
				sourceSimpleColumns.add((SimpleBusinessColumn)column);
			}
		}
		return sourceSimpleColumns;
	}
	
} //BusinessRelationshipImpl
