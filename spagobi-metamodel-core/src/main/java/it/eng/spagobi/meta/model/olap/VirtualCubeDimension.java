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
 * A representation of the model object '<em><b>Virtual Cube Dimension</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube <em>Virtual Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getDimension <em>Dimension</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeDimension()
 * @model
 * @generated
 */
public interface VirtualCubeDimension extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Virtual Cube</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Virtual Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Virtual Cube</em>' reference.
	 * @see #setVirtualCube(VirtualCube)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeDimension_VirtualCube()
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions
	 * @model opposite="dimensions"
	 * @generated
	 */
	VirtualCube getVirtualCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube <em>Virtual Cube</em>}' reference.
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
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeDimension_Cube()
	 * @model
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getCube <em>Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cube</em>' reference.
	 * @see #getCube()
	 * @generated
	 */
	void setCube(Cube value);

	/**
	 * Returns the value of the '<em><b>Dimension</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimension</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dimension</em>' reference.
	 * @see #setDimension(Dimension)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCubeDimension_Dimension()
	 * @model
	 * @generated
	 */
	Dimension getDimension();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getDimension <em>Dimension</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dimension</em>' reference.
	 * @see #getDimension()
	 * @generated
	 */
	void setDimension(Dimension value);

} // VirtualCubeDimension
