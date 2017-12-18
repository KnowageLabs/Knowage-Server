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
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.CalculatedBusinessColumn;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
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
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Business Column Set</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.impl.BusinessColumnSetImpl#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BusinessColumnSetImpl extends ModelObjectImpl implements BusinessColumnSet {
	/**
	 * The cached value of the '{@link #getColumns() <em>Columns</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumns()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessColumn> columns;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BusinessColumnSetImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.BUSINESS_COLUMN_SET;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessModel getModel() {
		if (eContainerFeatureID() != BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL) return null;
		return (BusinessModel)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetModel(BusinessModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newModel, BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setModel(BusinessModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject)newModel).eInverseAdd(this, BusinessModelPackage.BUSINESS_MODEL__TABLES, BusinessModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL, newModel, newModel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<BusinessColumn> getColumns() {
		if (columns == null) {
			columns = new EObjectContainmentWithInverseEList<BusinessColumn>(BusinessColumn.class, this, BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS, BusinessModelPackage.BUSINESS_COLUMN__TABLE);
		}
		return columns;
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetModel((BusinessModel)otherEnd, msgs);
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getColumns()).basicAdd(otherEnd, msgs);
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				return basicSetModel(null, msgs);
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				return ((InternalEList<?>)getColumns()).basicRemove(otherEnd, msgs);
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				return eInternalContainer().eInverseRemove(this, BusinessModelPackage.BUSINESS_MODEL__TABLES, BusinessModel.class, msgs);
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				return getModel();
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				return getColumns();
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				setModel((BusinessModel)newValue);
				return;
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				getColumns().clear();
				getColumns().addAll((Collection<? extends BusinessColumn>)newValue);
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				setModel((BusinessModel)null);
				return;
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				getColumns().clear();
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
			case BusinessModelPackage.BUSINESS_COLUMN_SET__MODEL:
				return getModel() != null;
			case BusinessModelPackage.BUSINESS_COLUMN_SET__COLUMNS:
				return columns != null && !columns.isEmpty();
		}
		return super.eIsSet(featureID);
	}
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	
	@Override
	public BusinessIdentifier getIdentifier() {
		return (getModel() != null)? getModel().getIdentifier(this): null;
	}
	
	
	@Override
	public SimpleBusinessColumn getSimpleBusinessColumnByUniqueName(String uniqueName) {
		
		if(uniqueName == null) {
			return null;
		}
		
		for(SimpleBusinessColumn column :  getSimpleBusinessColumns()) {
			if(uniqueName.equals( column.getUniqueName() )) {
				return column;
			}
		}
		return null;
	}
	
	@Override
	public List<SimpleBusinessColumn> getSimpleBusinessColumnsByName(String name) {
		
		List<SimpleBusinessColumn> columns = new ArrayList<SimpleBusinessColumn>();
		
		if(name == null) {
			return columns;
		}
		
		for(SimpleBusinessColumn column :  getSimpleBusinessColumns()) {
			if(name.equals( column.getName() )) {
				columns.add(column);
			}
		}
		
		return columns;
	}
	
	@Override
	public List<SimpleBusinessColumn> getSimpleBusinessColumnsByPhysicalColumn(String physicalTableName, String physicalColumnName) {
		
		List<SimpleBusinessColumn> columns = new ArrayList<SimpleBusinessColumn>();
		
		BusinessModel businessModel = getModel();
		if(businessModel == null) return columns;
		
		PhysicalModel physicalModel = businessModel.getPhysicalModel();
		if(physicalModel == null) return columns;
		
		PhysicalTable physicalTable = physicalModel.getTable(physicalTableName);
		if(physicalTable == null) return columns;
		
		PhysicalColumn physicalColumn = physicalTable.getColumn(physicalColumnName);
		if(physicalColumn == null) return columns;
		
		return getSimpleBusinessColumnsByPhysicalColumn(physicalColumn);
	}
	
	@Override
	public List<SimpleBusinessColumn> getSimpleBusinessColumnsByPhysicalColumn(PhysicalColumn physicalColumn) {
		
		List<SimpleBusinessColumn> columns = new ArrayList<SimpleBusinessColumn>();
		
		if(physicalColumn == null) {
			return columns;
		}
		
		for(SimpleBusinessColumn column :  getSimpleBusinessColumns()) {
			if(column.getPhysicalColumn().equals(physicalColumn)) {
				columns.add(column);
			} 
		}
		return columns;
	}
	
	@Override
	public List<SimpleBusinessColumn> getSimpleBusinessColumns() {
		EList<BusinessColumn> businessColumns = getColumns();
		List<SimpleBusinessColumn> simpleColumns = new ArrayList<SimpleBusinessColumn>();
		for (BusinessColumn column:businessColumns){
			if (column instanceof SimpleBusinessColumn){
				simpleColumns.add((SimpleBusinessColumn)column);
			}
		}
		return simpleColumns;
	}
	
	
	
	// -- deprecated  ---------
	
	@Override
	public SimpleBusinessColumn getSimpleBusinessColumn(String name) {
		for(int i = 0; i < getSimpleBusinessColumns().size(); i++) {
			if(getSimpleBusinessColumns().get(i).getName().equals(name)) {
				return getSimpleBusinessColumns().get(i);
			}
		}
		return null;
	}

	@Override
	public SimpleBusinessColumn getSimpleBusinessColumn(PhysicalColumn physicalColumn) {
		for(int i = 0; i < getSimpleBusinessColumns().size(); i++) {
			if(getSimpleBusinessColumns().get(i).getPhysicalColumn().equals(physicalColumn)) {
				return getSimpleBusinessColumns().get(i);
			} 
		}
		return null;
	}
	// -- deprecated  ---------
	
	
	public CalculatedBusinessColumn getCalculatedBusinessColumn(String name){
		for(int i = 0; i < getCalculatedBusinessColumns().size(); i++) {
			if(getCalculatedBusinessColumns().get(i).getName().equals(name)) {
				return getCalculatedBusinessColumns().get(i);
			}
		}
		return null;
	}

	
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getModel().getParentModel().getPropertyTypes();
	}

	@Override
	public List<BusinessRelationship> getRelationships() {
		List<BusinessRelationship> relationships = new ArrayList<BusinessRelationship>();
		
		Iterator<BusinessRelationship> it = getModel().getRelationships().iterator();
		
		while (it.hasNext()) {
			BusinessRelationship relationship = it.next();
			
			if ( (relationship.getSourceTable() != null)&& (relationship.getSourceTable().equals(this)) ){
				if (relationship.getDestinationTable() != null)
					relationships.add(relationship); 
			}
			else if ( (relationship.getDestinationTable() != null) && (relationship.getDestinationTable().equals(this)) ) {
				if (relationship.getSourceTable() != null)
					relationships.add(relationship);
			}
		}
		return relationships;
	}


	@Override
	public List<CalculatedBusinessColumn> getCalculatedBusinessColumns() {
		EList<BusinessColumn> businessColumns = getColumns();
		List<CalculatedBusinessColumn> calculatedColumns = new ArrayList<CalculatedBusinessColumn>();
		for (BusinessColumn column:businessColumns){
			if (column instanceof CalculatedBusinessColumn){
				calculatedColumns.add((CalculatedBusinessColumn)column);
			}
		}
		return calculatedColumns;
	}

} //BusinessColumnSetImpl
