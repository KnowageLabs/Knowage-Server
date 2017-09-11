/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.ModelObject;

import it.eng.spagobi.meta.model.business.BusinessColumn;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Measure</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Measure#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Measure#getColumn <em>Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getMeasure()
 * @model
 * @generated
 */
public interface Measure extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Cube</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Cube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cube</em>' container reference.
	 * @see #setCube(Cube)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getMeasure_Cube()
	 * @see it.eng.spagobi.meta.model.olap.Cube#getMeasures
	 * @model opposite="measures" transient="false"
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Measure#getCube <em>Cube</em>}' container reference.
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
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getMeasure_Column()
	 * @model
	 * @generated
	 */
	BusinessColumn getColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Measure#getColumn <em>Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column</em>' reference.
	 * @see #getColumn()
	 * @generated
	 */
	void setColumn(BusinessColumn value);

} // Measure
