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
 * A representation of the model object '<em><b>Physical Foreign Key</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getSourceTable <em>Source Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getSourceColumns <em>Source Columns</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getSourceName <em>Source Name</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getDestinationName <em>Destination Name</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getDestinationTable <em>Destination Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getDestinationColumns <em>Destination Columns</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getModel <em>Model</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey()
 * @model
 * @generated
 */
public interface PhysicalForeignKey extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Source Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Name</em>' attribute.
	 * @see #setSourceName(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_SourceName()
	 * @model
	 * @generated
	 */
	String getSourceName();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getSourceName <em>Source Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Name</em>' attribute.
	 * @see #getSourceName()
	 * @generated
	 */
	void setSourceName(String value);

	/**
	 * Returns the value of the '<em><b>Source Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Table</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Table</em>' reference.
	 * @see #setSourceTable(PhysicalTable)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_SourceTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getSourceTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getSourceTable <em>Source Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Table</em>' reference.
	 * @see #getSourceTable()
	 * @generated
	 */
	void setSourceTable(PhysicalTable value);

	/**
	 * Returns the value of the '<em><b>Source Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.physical.PhysicalColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Columns</em>' reference list.
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_SourceColumns()
	 * @model required="true"
	 * @generated
	 */
	EList<PhysicalColumn> getSourceColumns();

	/**
	 * Returns the value of the '<em><b>Destination Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Destination Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Destination Name</em>' attribute.
	 * @see #setDestinationName(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_DestinationName()
	 * @model
	 * @generated
	 */
	String getDestinationName();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getDestinationName <em>Destination Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Destination Name</em>' attribute.
	 * @see #getDestinationName()
	 * @generated
	 */
	void setDestinationName(String value);

	/**
	 * Returns the value of the '<em><b>Destination Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Destination Table</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Destination Table</em>' reference.
	 * @see #setDestinationTable(PhysicalTable)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_DestinationTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getDestinationTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getDestinationTable <em>Destination Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Destination Table</em>' reference.
	 * @see #getDestinationTable()
	 * @generated
	 */
	void setDestinationTable(PhysicalTable value);

	/**
	 * Returns the value of the '<em><b>Destination Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.physical.PhysicalColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Destination Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Destination Columns</em>' reference list.
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_DestinationColumns()
	 * @model required="true"
	 * @generated
	 */
	EList<PhysicalColumn> getDestinationColumns();

	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.physical.PhysicalModel#getForeignKeys <em>Foreign Keys</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(PhysicalModel)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalForeignKey_Model()
	 * @see it.eng.knowage.meta.model.physical.PhysicalModel#getForeignKeys
	 * @model opposite="foreignKeys" transient="false"
	 * @generated
	 */
	PhysicalModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalForeignKey#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(PhysicalModel value);

} // PhysicalForeignKey
