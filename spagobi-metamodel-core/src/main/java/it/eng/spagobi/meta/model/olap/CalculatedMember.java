/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.ModelObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Calculated Member</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getHierarchy <em>Hierarchy</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getCalculatedMember()
 * @model
 * @generated
 */
public interface CalculatedMember extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Cube</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Cube#getCalculatedMembers <em>Calculated Members</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cube</em>' reference.
	 * @see #setCube(Cube)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getCalculatedMember_Cube()
	 * @see it.eng.spagobi.meta.model.olap.Cube#getCalculatedMembers
	 * @model opposite="calculatedMembers"
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getCube <em>Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cube</em>' reference.
	 * @see #getCube()
	 * @generated
	 */
	void setCube(Cube value);

	/**
	 * Returns the value of the '<em><b>Hierarchy</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hierarchy</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hierarchy</em>' reference.
	 * @see #setHierarchy(Hierarchy)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getCalculatedMember_Hierarchy()
	 * @model
	 * @generated
	 */
	Hierarchy getHierarchy();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.CalculatedMember#getHierarchy <em>Hierarchy</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hierarchy</em>' reference.
	 * @see #getHierarchy()
	 * @generated
	 */
	void setHierarchy(Hierarchy value);

} // CalculatedMember
