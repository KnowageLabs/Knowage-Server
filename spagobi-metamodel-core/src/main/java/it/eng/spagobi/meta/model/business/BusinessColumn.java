/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.ModelObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Business Column</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.spagobi.meta.model.business.BusinessColumn#getTable <em>Table</em>}</li>
 * </ul>
 * </p>
 * 
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessColumn()
 * @model
 * @generated
 */
public interface BusinessColumn extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Table</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getColumns <em>Columns</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Table</em>' container reference.
	 * @see #setTable(BusinessColumnSet)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessColumn_Table()
	 * @see it.eng.spagobi.meta.model.business.BusinessColumnSet#getColumns
	 * @model opposite="columns" required="true" transient="false"
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessColumn#getTable <em>Table</em>}' container reference. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Table</em>' container reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	// =========================================================================
	// Utility methods
	// =========================================================================

	boolean isIdentifier();

	boolean isFilteredByProfileAttribute();

	boolean isPartOfCompositeIdentifier();

	boolean isFilteredByRoleVisibility();

} // BusinessColumn
