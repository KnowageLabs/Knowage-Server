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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Physical Primary Key</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalPrimaryKey#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalPrimaryKey#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalPrimaryKey#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalPrimaryKey()
 * @model
 * @generated
 */
public interface PhysicalPrimaryKey extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.physical.PhysicalModel#getPrimaryKeys <em>Primary Keys</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(PhysicalModel)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalPrimaryKey_Model()
	 * @see it.eng.knowage.meta.model.physical.PhysicalModel#getPrimaryKeys
	 * @model opposite="primaryKeys" required="true" transient="false"
	 * @generated
	 */
	PhysicalModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalPrimaryKey#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(PhysicalModel value);

	/**
	 * Returns the value of the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Table</em>' reference.
	 * @see #setTable(PhysicalTable)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalPrimaryKey_Table()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalPrimaryKey#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(PhysicalTable value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.physical.PhysicalColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' reference list.
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalPrimaryKey_Columns()
	 * @model required="true"
	 * @generated
	 */
	EList<PhysicalColumn> getColumns();

} // PhysicalPrimaryKey
