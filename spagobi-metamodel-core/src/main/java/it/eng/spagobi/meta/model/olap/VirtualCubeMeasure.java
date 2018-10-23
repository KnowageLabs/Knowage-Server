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
 * A representation of the model object '<em><b>Virtual Cube Measure</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getMeasure <em>Measure</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure()
 * @model
 * @generated
 */
public interface VirtualCubeMeasure extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Virtual Cube</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Virtual Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Virtual Cube</em>' reference.
	 * @see #setVirtualCube(VirtualCube)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_VirtualCube()
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getMeasures
	 * @model opposite="measures"
	 * @generated
	 */
	VirtualCube getVirtualCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Virtual Cube</em>' reference.
	 * @see #getVirtualCube()
	 * @generated
	 */
	void setVirtualCube(VirtualCube value);

	/**
	 * Returns the value of the '<em><b>Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cube</em>' reference.
	 * @see #setCube(Cube)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_Cube()
	 * @model
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getCube <em>Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cube</em>' reference.
	 * @see #getCube()
	 * @generated
	 */
	void setCube(Cube value);

	/**
	 * Returns the value of the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Measure</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Measure</em>' reference.
	 * @see #setMeasure(Measure)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_Measure()
	 * @model
	 * @generated
	 */
	Measure getMeasure();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getMeasure <em>Measure</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Measure</em>' reference.
	 * @see #getMeasure()
	 * @generated
	 */
	void setMeasure(Measure value);

} // VirtualCubeMeasure
