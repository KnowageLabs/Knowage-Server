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
package it.eng.knowage.meta.model.business;

import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business View Inner Join Relationship</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable <em>Source Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable <em>Destination Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getSourceColumns <em>Source Columns</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationColumns <em>Destination Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship()
 * @model
 * @generated
 */
public interface BusinessViewInnerJoinRelationship extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.business.BusinessModel#getJoinRelationships <em>Join Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_Model()
	 * @see it.eng.knowage.meta.model.business.BusinessModel#getJoinRelationships
	 * @model opposite="joinRelationships" required="true" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

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
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_SourceTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getSourceTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable <em>Source Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Table</em>' reference.
	 * @see #getSourceTable()
	 * @generated
	 */
	void setSourceTable(PhysicalTable value);

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
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_DestinationTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getDestinationTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable <em>Destination Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Destination Table</em>' reference.
	 * @see #getDestinationTable()
	 * @generated
	 */
	void setDestinationTable(PhysicalTable value);

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
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_SourceColumns()
	 * @model
	 * @generated
	 */
	EList<PhysicalColumn> getSourceColumns();

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
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_DestinationColumns()
	 * @model
	 * @generated
	 */
	EList<PhysicalColumn> getDestinationColumns();

} // BusinessViewInnerJoinRelationship
