/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business Identifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getColumns <em>Columns</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey <em>Physical Primary Key</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessIdentifier()
 * @model
 * @generated
 */
public interface BusinessIdentifier extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.business.BusinessModel#getIdentifiers <em>Identifiers</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Model()
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getIdentifiers
	 * @model opposite="identifiers" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

	/**
	 * Returns the value of the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Table</em>' reference.
	 * @see #setTable(BusinessColumnSet)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Table()
	 * @model required="true"
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.business.BusinessColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' reference list.
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Columns()
	 * @model required="true"
	 * @generated
	 */
	EList<BusinessColumn> getColumns();

	/**
	 * Returns the value of the '<em><b>Physical Primary Key</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Primary Key</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Primary Key</em>' reference.
	 * @see #setPhysicalPrimaryKey(PhysicalPrimaryKey)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessIdentifier_PhysicalPrimaryKey()
	 * @model
	 * @generated
	 */
	PhysicalPrimaryKey getPhysicalPrimaryKey();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey <em>Physical Primary Key</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Primary Key</em>' reference.
	 * @see #getPhysicalPrimaryKey()
	 * @generated
	 */
	void setPhysicalPrimaryKey(PhysicalPrimaryKey value);
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	
	List<SimpleBusinessColumn> getSimpleBusinessColumns();

} // BusinessIdentifier
