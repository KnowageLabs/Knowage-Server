/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.physical;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelObject;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Physical Model</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getDatabaseName <em>Database Name</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getDatabaseVersion <em>Database Version</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getCatalog <em>Catalog</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getSchema <em>Schema</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getParentModel <em>Parent Model</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getTables <em>Tables</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getPrimaryKeys <em>Primary Keys</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getForeignKeys <em>Foreign Keys</em>}</li>
 * </ul>
 * </p>
 * 
 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel()
 * @model
 * @generated
 */
public interface PhysicalModel extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Database Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Database Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Database Name</em>' attribute.
	 * @see #setDatabaseName(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_DatabaseName()
	 * @model
	 * @generated
	 */
	String getDatabaseName();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getDatabaseName <em>Database Name</em>}' attribute. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Database Name</em>' attribute.
	 * @see #getDatabaseName()
	 * @generated
	 */
	void setDatabaseName(String value);

	/**
	 * Returns the value of the '<em><b>Database Version</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Database Version</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Database Version</em>' attribute.
	 * @see #setDatabaseVersion(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_DatabaseVersion()
	 * @model
	 * @generated
	 */
	String getDatabaseVersion();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getDatabaseVersion <em>Database Version</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Database Version</em>' attribute.
	 * @see #getDatabaseVersion()
	 * @generated
	 */
	void setDatabaseVersion(String value);

	/**
	 * Returns the value of the '<em><b>Catalog</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Catalog</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Catalog</em>' attribute.
	 * @see #setCatalog(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_Catalog()
	 * @model
	 * @generated
	 */
	String getCatalog();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getCatalog <em>Catalog</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Catalog</em>' attribute.
	 * @see #getCatalog()
	 * @generated
	 */
	void setCatalog(String value);

	/**
	 * Returns the value of the '<em><b>Schema</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Schema</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Schema</em>' attribute.
	 * @see #setSchema(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_Schema()
	 * @model
	 * @generated
	 */
	String getSchema();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getSchema <em>Schema</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Schema</em>' attribute.
	 * @see #getSchema()
	 * @generated
	 */
	void setSchema(String value);

	/**
	 * Returns the value of the '<em><b>Parent Model</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.Model#getPhysicalModels <em>Physical Models</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent Model</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Parent Model</em>' container reference.
	 * @see #setParentModel(Model)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_ParentModel()
	 * @see it.eng.spagobi.meta.model.Model#getPhysicalModels
	 * @model opposite="physicalModels" required="true" transient="false"
	 * @generated
	 */
	Model getParentModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getParentModel <em>Parent Model</em>}' container reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Parent Model</em>' container reference.
	 * @see #getParentModel()
	 * @generated
	 */
	void setParentModel(Model value);

	/**
	 * Returns the value of the '<em><b>Tables</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalTable}. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalTable#getModel <em>Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tables</em>' containment reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Tables</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_Tables()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalTable#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<PhysicalTable> getTables();

	/**
	 * Returns the value of the '<em><b>Primary Keys</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey}. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey#getModel <em>Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Primary Keys</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Primary Keys</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_PrimaryKeys()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<PhysicalPrimaryKey> getPrimaryKeys();

	/**
	 * Returns the value of the '<em><b>Foreign Keys</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalForeignKey}. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalForeignKey#getModel <em>Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Foreign Keys</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Foreign Keys</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalModel_ForeignKeys()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalForeignKey#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<PhysicalForeignKey> getForeignKeys();

	// =========================================================================
	// Utility methods
	// =========================================================================

	PhysicalTable getTable(String name);

	PhysicalPrimaryKey getPrimaryKey(String name);

	PhysicalPrimaryKey getPrimaryKey(PhysicalTable table);

	List<PhysicalForeignKey> getForeignKeys(PhysicalTable table);

	/**
	 * Return foreign keys that have the passed tale as source or destination table
	 * 
	 * @return
	 */
	List<PhysicalForeignKey> getForeignKeysInvolvingTable(PhysicalTable table);

} // PhysicalModel
