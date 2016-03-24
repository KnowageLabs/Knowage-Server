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
package it.eng.knowage.meta.model.physical;

import it.eng.knowage.meta.model.ModelObject;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Physical Table</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalTable#getComment <em>Comment</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalTable#getType <em>Type</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalTable#getModel <em>Model</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalTable#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 * 
 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalTable()
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
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalTable#getComment <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!--
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
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalTable#getType <em>Type</em>}' attribute. <!-- begin-user-doc --> <!--
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
	 * {@link it.eng.knowage.meta.model.physical.PhysicalModel#getTables <em>Tables</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(PhysicalModel)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Model()
	 * @see it.eng.knowage.meta.model.physical.PhysicalModel#getTables
	 * @model opposite="tables" required="true" transient="false"
	 * @generated
	 */
	PhysicalModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalTable#getModel <em>Model</em>}' container reference. <!-- begin-user-doc -->
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
	 * {@link it.eng.knowage.meta.model.physical.PhysicalColumn}. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.physical.PhysicalColumn#getTable <em>Table</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Columns</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalTable_Columns()
	 * @see it.eng.knowage.meta.model.physical.PhysicalColumn#getTable
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
