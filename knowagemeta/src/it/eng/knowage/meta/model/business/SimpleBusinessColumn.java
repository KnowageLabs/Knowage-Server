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

import it.eng.knowage.meta.model.physical.PhysicalColumn;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Simple Business Column</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.business.SimpleBusinessColumn#getPhysicalColumn <em>Physical Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getSimpleBusinessColumn()
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
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getSimpleBusinessColumn_PhysicalColumn()
	 * @model required="true"
	 * @generated
	 */
	PhysicalColumn getPhysicalColumn();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.SimpleBusinessColumn#getPhysicalColumn <em>Physical Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Column</em>' reference.
	 * @see #getPhysicalColumn()
	 * @generated
	 */
	void setPhysicalColumn(PhysicalColumn value);

} // SimpleBusinessColumn
