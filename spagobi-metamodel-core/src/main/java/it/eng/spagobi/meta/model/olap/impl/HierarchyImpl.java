/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.impl;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.business.impl.BusinessTableImpl;

import it.eng.spagobi.meta.model.impl.ModelObjectImpl;

import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.Hierarchy;
import it.eng.spagobi.meta.model.olap.Level;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;
import it.eng.spagobi.meta.model.physical.PhysicalTable;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Hierarchy</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.HierarchyImpl#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.HierarchyImpl#getDimension <em>Dimension</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.HierarchyImpl#getLevels <em>Levels</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class HierarchyImpl extends ModelObjectImpl implements Hierarchy {
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
	 * The cached value of the '{@link #getLevels() <em>Levels</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLevels()
	 * @generated
	 * @ordered
	 */
	protected EList<Level> levels;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HierarchyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.HIERARCHY;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.HIERARCHY__TABLE, oldTable, table));
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
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.HIERARCHY__TABLE, oldTable, table));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Dimension getDimension() {
		if (eContainerFeatureID() != OlapModelPackage.HIERARCHY__DIMENSION) return null;
		return (Dimension)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDimension(Dimension newDimension, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newDimension, OlapModelPackage.HIERARCHY__DIMENSION, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDimension(Dimension newDimension) {
		if (newDimension != eInternalContainer() || (eContainerFeatureID() != OlapModelPackage.HIERARCHY__DIMENSION && newDimension != null)) {
			if (EcoreUtil.isAncestor(this, newDimension))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newDimension != null)
				msgs = ((InternalEObject)newDimension).eInverseAdd(this, OlapModelPackage.DIMENSION__HIERARCHIES, Dimension.class, msgs);
			msgs = basicSetDimension(newDimension, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.HIERARCHY__DIMENSION, newDimension, newDimension));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Level> getLevels() {
		if (levels == null) {
			levels = new EObjectContainmentWithInverseEList<Level>(Level.class, this, OlapModelPackage.HIERARCHY__LEVELS, OlapModelPackage.LEVEL__HIERARCHY);
		}
		return levels;
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
			case OlapModelPackage.HIERARCHY__DIMENSION:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetDimension((Dimension)otherEnd, msgs);
			case OlapModelPackage.HIERARCHY__LEVELS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getLevels()).basicAdd(otherEnd, msgs);
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
			case OlapModelPackage.HIERARCHY__DIMENSION:
				return basicSetDimension(null, msgs);
			case OlapModelPackage.HIERARCHY__LEVELS:
				return ((InternalEList<?>)getLevels()).basicRemove(otherEnd, msgs);
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
			case OlapModelPackage.HIERARCHY__DIMENSION:
				return eInternalContainer().eInverseRemove(this, OlapModelPackage.DIMENSION__HIERARCHIES, Dimension.class, msgs);
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
			case OlapModelPackage.HIERARCHY__TABLE:
				if (resolve) return getTable();
				return basicGetTable();
			case OlapModelPackage.HIERARCHY__DIMENSION:
				return getDimension();
			case OlapModelPackage.HIERARCHY__LEVELS:
				return getLevels();
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
			case OlapModelPackage.HIERARCHY__TABLE:
				setTable((BusinessColumnSet)newValue);
				return;
			case OlapModelPackage.HIERARCHY__DIMENSION:
				setDimension((Dimension)newValue);
				return;
			case OlapModelPackage.HIERARCHY__LEVELS:
				getLevels().clear();
				getLevels().addAll((Collection<? extends Level>)newValue);
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
			case OlapModelPackage.HIERARCHY__TABLE:
				setTable((BusinessColumnSet)null);
				return;
			case OlapModelPackage.HIERARCHY__DIMENSION:
				setDimension((Dimension)null);
				return;
			case OlapModelPackage.HIERARCHY__LEVELS:
				getLevels().clear();
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
			case OlapModelPackage.HIERARCHY__TABLE:
				return table != null;
			case OlapModelPackage.HIERARCHY__DIMENSION:
				return getDimension() != null;
			case OlapModelPackage.HIERARCHY__LEVELS:
				return levels != null && !levels.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getDimension().getModel().getParentModel().getPropertyTypes();
	}
	

	
	public IDataStore getMembers(String levelName){
		BusinessColumnSet table;
		PhysicalTable physicalTable;
		String tableName,  query, columnName;
		
		table = getTable();
		EList<Level> levels = getLevels();
		for(int i=0; i<levels.size(); i++){
			Level l = levels.get(i);
			if(l.getName().equals(levelName)){
				columnName = l.getColumn().getUniqueName();
				
				physicalTable = ((BusinessTableImpl)table).getPhysicalTable();
				tableName = physicalTable.getName();
			
				query = "Select distinct "+columnName+" as val from "+tableName;
				
				JDBCDataSet dataSet = new JDBCDataSet();
				dataSet.setDataSource(getDimension().getModel().getParentModel().getDataSource());
				dataSet.setQuery(query);
				dataSet.loadData();
				return dataSet.getDataStore();
			}
		}
		
		throw new RuntimeException("The levelName is not a name valid for this hierarchy");

	}
	/**
	 * columnName1 must is in a lower level of the hierarchy
	 */
	public Map<Object, Object> getMembersMapBetweenLevels(String columnName1, String columnName2){
		BusinessColumnSet table;
		PhysicalTable physicalTable;
		String tableName,  query;
		Map<Object, Object> toReturn= new HashMap<Object, Object> ();
		
		table = getTable();
		physicalTable = ((BusinessTableImpl)table).getPhysicalTable();
		tableName = physicalTable.getName();
	
		query = "Select distinct "+columnName1+" as col1 ,"+columnName2+" as col2 from "+tableName;
		
		JDBCDataSet dataSet = new JDBCDataSet();
		dataSet.setDataSource(getDimension().getModel().getParentModel().getDataSource());
		dataSet.setQuery(query);
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		
		for(int i=0; i<dataStore.getRecordsCount(); i++){
			IRecord aRecord = dataStore.getRecordAt(i);
			Object obj1 = aRecord.getFieldAt(0).getValue();
			Object obj2 = aRecord.getFieldAt(1).getValue();
			toReturn.put(obj1, obj2);
		}
		
		return toReturn;
	}

	/** 
	 * 	Return the values of the siblingColumnName on the table used for this hierarchy
	 * 	
	 */
	@Override
	public IDataStore getSiblingValues(String siblingColumnName) {
		BusinessColumnSet table;
		PhysicalTable physicalTable;
		String tableName, query;

		table = getTable();

		physicalTable = ((BusinessTableImpl)table).getPhysicalTable();
		tableName = physicalTable.getName();

		query = "Select distinct "+siblingColumnName+" as val from "+tableName;

		JDBCDataSet dataSet = new JDBCDataSet();
		dataSet.setDataSource(getDimension().getModel().getParentModel().getDataSource());
		dataSet.setQuery(query);
		dataSet.loadData();
		return dataSet.getDataStore();


	}

	/* 
	 * Return the corresponding values on the default level column for each sibling column values
	 */
	@Override
	public Map<Object, Object> getMembersAndSibling(String levelName,
			String siblingColumnName) {
		String levelColumnName;
		
		table = getTable();
		EList<Level> levels = getLevels();
		for(int i=0; i<levels.size(); i++){
			Level l = levels.get(i);
			if(l.getName().equals(levelName)){
				levelColumnName = l.getColumn().getUniqueName();

				return getMembersMapBetweenLevels(siblingColumnName,levelColumnName);
			}
		}
		
		throw new RuntimeException("The levelName is not a name valid for this hierarchy");
	}

} //HierarchyImpl
