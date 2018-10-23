/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.physical;

import it.eng.spagobi.meta.model.ModelObject;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Physical Table</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getComment <em>Comment</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getType <em>Type</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getModel <em>Model</em>}</li>
 * <li>{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 * 
 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalTable()
 * @model
 * @generated
 */
public interface PhysicalTable extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getComment <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getType <em>Type</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalModel#getTables <em>Tables</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(PhysicalModel)
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Model()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModel#getTables
	 * @model opposite="tables" required="true" transient="false"
	 * @generated
	 */
	PhysicalModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.physical.PhysicalTable#getModel <em>Model</em>}' container reference. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(PhysicalModel value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalColumn}. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.physical.PhysicalColumn#getTable <em>Table</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Columns</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Columns()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalColumn#getTable
	 * @model opposite="table" containment="true"
	 * @generated
	 */
	EList<PhysicalColumn> getColumns();

	// =========================================================================
	// Utility methods
	// =========================================================================

	public PhysicalPrimaryKey getPrimaryKey();

	public PhysicalColumn getColumn(String name);

	public List<PhysicalForeignKey> getForeignKeys();

	public boolean containsAllNotDeleted(List<PhysicalColumn> physicalColumns);

	/**
	 * Return foreign keys that have this table as source or destination table
	 * 
	 * @return
	 */
	public List<PhysicalForeignKey> getForeignKeysInvolvingTable();

} // PhysicalTable
