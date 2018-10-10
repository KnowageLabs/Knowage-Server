/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.physical.impl;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.impl.ModelObjectImpl;
import it.eng.spagobi.meta.model.physical.PhysicalForeignKey;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalModelPackage;
import it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey;
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
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Physical Model</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getDatabaseName <em>Database Name</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getDatabaseVersion <em>Database Version</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getCatalog <em>Catalog</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getSchema <em>Schema</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getParentModel <em>Parent Model</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getTables <em>Tables</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getPrimaryKeys <em>Primary Keys</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.impl.PhysicalModelImpl#getForeignKeys <em>Foreign Keys</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class PhysicalModelImpl extends ModelObjectImpl implements PhysicalModel {
	/**
	 * The default value of the '{@link #getDatabaseName() <em>Database Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getDatabaseName()
	 * @generated
	 * @ordered
	 */
	protected static final String DATABASE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDatabaseName() <em>Database Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getDatabaseName()
	 * @generated
	 * @ordered
	 */
	protected String databaseName = DATABASE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDatabaseVersion() <em>Database Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getDatabaseVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String DATABASE_VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDatabaseVersion() <em>Database Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getDatabaseVersion()
	 * @generated
	 * @ordered
	 */
	protected String databaseVersion = DATABASE_VERSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getCatalog() <em>Catalog</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getCatalog()
	 * @generated
	 * @ordered
	 */
	protected static final String CATALOG_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCatalog() <em>Catalog</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getCatalog()
	 * @generated
	 * @ordered
	 */
	protected String catalog = CATALOG_EDEFAULT;

	/**
	 * The default value of the '{@link #getSchema() <em>Schema</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected static final String SCHEMA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSchema() <em>Schema</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected String schema = SCHEMA_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTables() <em>Tables</em>}' containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTables()
	 * @generated
	 * @ordered
	 */
	protected EList<PhysicalTable> tables;

	/**
	 * The cached value of the '{@link #getPrimaryKeys() <em>Primary Keys</em>}' containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getPrimaryKeys()
	 * @generated
	 * @ordered
	 */
	protected EList<PhysicalPrimaryKey> primaryKeys;

	/**
	 * The cached value of the '{@link #getForeignKeys() <em>Foreign Keys</em>}' containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getForeignKeys()
	 * @generated
	 * @ordered
	 */
	protected EList<PhysicalForeignKey> foreignKeys;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PhysicalModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PhysicalModelPackage.Literals.PHYSICAL_MODEL;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setDatabaseName(String newDatabaseName) {
		String oldDatabaseName = databaseName;
		databaseName = newDatabaseName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_NAME, oldDatabaseName, databaseName));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getDatabaseVersion() {
		return databaseVersion;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setDatabaseVersion(String newDatabaseVersion) {
		String oldDatabaseVersion = databaseVersion;
		databaseVersion = newDatabaseVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_VERSION, oldDatabaseVersion, databaseVersion));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getCatalog() {
		return catalog;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setCatalog(String newCatalog) {
		String oldCatalog = catalog;
		catalog = newCatalog;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_MODEL__CATALOG, oldCatalog, catalog));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getSchema() {
		return schema;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setSchema(String newSchema) {
		String oldSchema = schema;
		schema = newSchema;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_MODEL__SCHEMA, oldSchema, schema));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Model getParentModel() {
		if (eContainerFeatureID() != PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL)
			return null;
		return (Model) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetParentModel(Model newParentModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newParentModel, PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setParentModel(Model newParentModel) {
		if (newParentModel != eInternalContainer() || (eContainerFeatureID() != PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL && newParentModel != null)) {
			if (EcoreUtil.isAncestor(this, newParentModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParentModel != null)
				msgs = ((InternalEObject) newParentModel).eInverseAdd(this, ModelPackage.MODEL__PHYSICAL_MODELS, Model.class, msgs);
			msgs = basicSetParentModel(newParentModel, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL, newParentModel, newParentModel));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<PhysicalTable> getTables() {
		if (tables == null) {
			tables = new EObjectContainmentWithInverseEList<PhysicalTable>(PhysicalTable.class, this, PhysicalModelPackage.PHYSICAL_MODEL__TABLES,
					PhysicalModelPackage.PHYSICAL_TABLE__MODEL);
		}
		return tables;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<PhysicalPrimaryKey> getPrimaryKeys() {
		if (primaryKeys == null) {
			primaryKeys = new EObjectContainmentWithInverseEList<PhysicalPrimaryKey>(PhysicalPrimaryKey.class, this,
					PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS, PhysicalModelPackage.PHYSICAL_PRIMARY_KEY__MODEL);
		}
		return primaryKeys;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<PhysicalForeignKey> getForeignKeys() {
		if (foreignKeys == null) {
			foreignKeys = new EObjectContainmentWithInverseEList<PhysicalForeignKey>(PhysicalForeignKey.class, this,
					PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS, PhysicalModelPackage.PHYSICAL_FOREIGN_KEY__MODEL);
		}
		return foreignKeys;
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
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			return basicSetParentModel((Model) otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getTables()).basicAdd(otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getPrimaryKeys()).basicAdd(otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getForeignKeys()).basicAdd(otherEnd, msgs);
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
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			return basicSetParentModel(null, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			return ((InternalEList<?>) getTables()).basicRemove(otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			return ((InternalEList<?>) getPrimaryKeys()).basicRemove(otherEnd, msgs);
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			return ((InternalEList<?>) getForeignKeys()).basicRemove(otherEnd, msgs);
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
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			return eInternalContainer().eInverseRemove(this, ModelPackage.MODEL__PHYSICAL_MODELS, Model.class, msgs);
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
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_NAME:
			return getDatabaseName();
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_VERSION:
			return getDatabaseVersion();
		case PhysicalModelPackage.PHYSICAL_MODEL__CATALOG:
			return getCatalog();
		case PhysicalModelPackage.PHYSICAL_MODEL__SCHEMA:
			return getSchema();
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			return getParentModel();
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			return getTables();
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			return getPrimaryKeys();
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			return getForeignKeys();
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
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_NAME:
			setDatabaseName((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_VERSION:
			setDatabaseVersion((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__CATALOG:
			setCatalog((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__SCHEMA:
			setSchema((String) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			setParentModel((Model) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			getTables().clear();
			getTables().addAll((Collection<? extends PhysicalTable>) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			getPrimaryKeys().clear();
			getPrimaryKeys().addAll((Collection<? extends PhysicalPrimaryKey>) newValue);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			getForeignKeys().clear();
			getForeignKeys().addAll((Collection<? extends PhysicalForeignKey>) newValue);
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
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_NAME:
			setDatabaseName(DATABASE_NAME_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_VERSION:
			setDatabaseVersion(DATABASE_VERSION_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__CATALOG:
			setCatalog(CATALOG_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__SCHEMA:
			setSchema(SCHEMA_EDEFAULT);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			setParentModel((Model) null);
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			getTables().clear();
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			getPrimaryKeys().clear();
			return;
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			getForeignKeys().clear();
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
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_NAME:
			return DATABASE_NAME_EDEFAULT == null ? databaseName != null : !DATABASE_NAME_EDEFAULT.equals(databaseName);
		case PhysicalModelPackage.PHYSICAL_MODEL__DATABASE_VERSION:
			return DATABASE_VERSION_EDEFAULT == null ? databaseVersion != null : !DATABASE_VERSION_EDEFAULT.equals(databaseVersion);
		case PhysicalModelPackage.PHYSICAL_MODEL__CATALOG:
			return CATALOG_EDEFAULT == null ? catalog != null : !CATALOG_EDEFAULT.equals(catalog);
		case PhysicalModelPackage.PHYSICAL_MODEL__SCHEMA:
			return SCHEMA_EDEFAULT == null ? schema != null : !SCHEMA_EDEFAULT.equals(schema);
		case PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL:
			return getParentModel() != null;
		case PhysicalModelPackage.PHYSICAL_MODEL__TABLES:
			return tables != null && !tables.isEmpty();
		case PhysicalModelPackage.PHYSICAL_MODEL__PRIMARY_KEYS:
			return primaryKeys != null && !primaryKeys.isEmpty();
		case PhysicalModelPackage.PHYSICAL_MODEL__FOREIGN_KEYS:
			return foreignKeys != null && !foreignKeys.isEmpty();
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
		result.append(" (databaseName: ");
		result.append(databaseName);
		result.append(", databaseVersion: ");
		result.append(databaseVersion);
		result.append(", catalog: ");
		result.append(catalog);
		result.append(", schema: ");
		result.append(schema);
		result.append(')');
		return result.toString();
	}

	// =========================================================================
	// Utility methods
	// =========================================================================

	@Override
	public List<PhysicalForeignKey> getForeignKeys(PhysicalTable table) {
		EList<PhysicalForeignKey> foreignKeys = getForeignKeys();
		List<PhysicalForeignKey> outputForeignKeys = new ArrayList<PhysicalForeignKey>();
		for (PhysicalForeignKey fk : foreignKeys) {
			if (fk.getSourceTable() == table) {
				outputForeignKeys.add(fk);
			}
		}
		return outputForeignKeys;
	}

	@Override
	public List<PhysicalForeignKey> getForeignKeysInvolvingTable(PhysicalTable table) {
		EList<PhysicalForeignKey> foreignKeys = getForeignKeys();
		List<PhysicalForeignKey> outputForeignKeys = new ArrayList<PhysicalForeignKey>();
		for (PhysicalForeignKey fk : foreignKeys) {
			if (fk.getSourceTable() == table || fk.getDestinationTable() == table) {
				outputForeignKeys.add(fk);
			}
		}
		return outputForeignKeys;
	}

	@Override
	public PhysicalTable getTable(String name) {
		PhysicalTable table;
		Iterator<PhysicalTable> it = getTables().iterator();
		while (it.hasNext()) {
			table = it.next();
			if (name.equals(table.getName())) {
				return table;
			}
		}
		return null;
	}

	@Override
	public PhysicalPrimaryKey getPrimaryKey(String name) {
		// assert name not null
		for (PhysicalPrimaryKey primaryKey : getPrimaryKeys()) {
			name.equals(primaryKey.getName());
		}
		return null;
	}

	@Override
	public PhysicalPrimaryKey getPrimaryKey(PhysicalTable table) {
		// assert table not null
		for (PhysicalPrimaryKey primaryKey : getPrimaryKeys()) {
			if (table.equals(primaryKey.getTable())) {
				return primaryKey;
			}
		}

		return null;
	}

	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getParentModel().getPropertyTypes();
	}

} // PhysicalModelImpl
