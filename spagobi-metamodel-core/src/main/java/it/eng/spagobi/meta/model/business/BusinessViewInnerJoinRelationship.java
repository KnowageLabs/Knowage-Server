/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business View Inner Join Relationship</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable <em>Source Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable <em>Destination Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceColumns <em>Source Columns</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationColumns <em>Destination Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship()
 * @model
 * @generated
 */
public interface BusinessViewInnerJoinRelationship extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.business.BusinessModel#getJoinRelationships <em>Join Relationships</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_Model()
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getJoinRelationships
	 * @model opposite="joinRelationships" required="true" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getModel <em>Model</em>}' container reference.
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
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_SourceTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getSourceTable();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getSourceTable <em>Source Table</em>}' reference.
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
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_DestinationTable()
	 * @model required="true"
	 * @generated
	 */
	PhysicalTable getDestinationTable();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship#getDestinationTable <em>Destination Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Destination Table</em>' reference.
	 * @see #getDestinationTable()
	 * @generated
	 */
	void setDestinationTable(PhysicalTable value);

	/**
	 * Returns the value of the '<em><b>Source Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.physical.PhysicalColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Columns</em>' reference list.
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_SourceColumns()
	 * @model
	 * @generated
	 */
	EList<PhysicalColumn> getSourceColumns();

	/**
	 * Returns the value of the '<em><b>Destination Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.physical.PhysicalColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Destination Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Destination Columns</em>' reference list.
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessViewInnerJoinRelationship_DestinationColumns()
	 * @model
	 * @generated
	 */
	EList<PhysicalColumn> getDestinationColumns();

} // BusinessViewInnerJoinRelationship
