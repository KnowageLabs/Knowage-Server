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
package it.eng.knowage.meta.model.olap;

import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessColumn;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Measure</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.olap.Measure#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Measure#getColumn <em>Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getMeasure()
 * @model
 * @generated
 */
public interface Measure extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Cube</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.Cube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cube</em>' container reference.
	 * @see #setCube(Cube)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getMeasure_Cube()
	 * @see it.eng.knowage.meta.model.olap.Cube#getMeasures
	 * @model opposite="measures" transient="false"
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Measure#getCube <em>Cube</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cube</em>' container reference.
	 * @see #getCube()
	 * @generated
	 */
	void setCube(Cube value);

	/**
	 * Returns the value of the '<em><b>Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Column</em>' reference.
	 * @see #setColumn(BusinessColumn)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getMeasure_Column()
	 * @model
	 * @generated
	 */
	BusinessColumn getColumn();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Measure#getColumn <em>Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column</em>' reference.
	 * @see #getColumn()
	 * @generated
	 */
	void setColumn(BusinessColumn value);

} // Measure
