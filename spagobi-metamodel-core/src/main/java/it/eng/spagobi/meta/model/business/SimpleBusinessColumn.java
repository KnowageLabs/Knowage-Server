/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.physical.PhysicalColumn;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Simple Business Column</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.SimpleBusinessColumn#getPhysicalColumn <em>Physical Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getSimpleBusinessColumn()
 * @model
 * @generated
 */
public interface SimpleBusinessColumn extends BusinessColumn {
	/**
	 * Returns the value of the '<em><b>Physical Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Column</em>' reference.
	 * @see #setPhysicalColumn(PhysicalColumn)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getSimpleBusinessColumn_PhysicalColumn()
	 * @model required="true"
	 * @generated
	 */
	PhysicalColumn getPhysicalColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.SimpleBusinessColumn#getPhysicalColumn <em>Physical Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Column</em>' reference.
	 * @see #getPhysicalColumn()
	 * @generated
	 */
	void setPhysicalColumn(PhysicalColumn value);

} // SimpleBusinessColumn
