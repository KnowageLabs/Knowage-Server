/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.ModelObject;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Virtual Cube</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCube#getCubes <em>Cubes</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions <em>Dimensions</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCube#getMeasures <em>Measures</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCube#getCalculatedMembers <em>Calculated Members</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.VirtualCube#getModel <em>Model</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube()
 * @model
 * @generated
 */
public interface VirtualCube extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Cubes</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.Cube}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cubes</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cubes</em>' reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube_Cubes()
	 * @model
	 * @generated
	 */
	EList<Cube> getCubes();

	/**
	 * Returns the value of the '<em><b>Dimensions</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimensions</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dimensions</em>' reference.
	 * @see #setDimensions(VirtualCubeDimension)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube_Dimensions()
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeDimension#getVirtualCube
	 * @model opposite="virtualCube"
	 * @generated
	 */
	VirtualCubeDimension getDimensions();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getDimensions <em>Dimensions</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dimensions</em>' reference.
	 * @see #getDimensions()
	 * @generated
	 */
	void setDimensions(VirtualCubeDimension value);

	/**
	 * Returns the value of the '<em><b>Measures</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Measures</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Measures</em>' reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube_Measures()
	 * @see it.eng.spagobi.meta.model.olap.VirtualCubeMeasure#getVirtualCube
	 * @model opposite="virtualCube"
	 * @generated
	 */
	EList<VirtualCubeMeasure> getMeasures();

	/**
	 * Returns the value of the '<em><b>Calculated Members</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.CalculatedMember}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calculated Members</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calculated Members</em>' reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube_CalculatedMembers()
	 * @model
	 * @generated
	 */
	EList<CalculatedMember> getCalculatedMembers();

	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.OlapModel#getVirtualCubes <em>Virtual Cubes</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(OlapModel)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getVirtualCube_Model()
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getVirtualCubes
	 * @model opposite="virtualCubes" transient="false"
	 * @generated
	 */
	OlapModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(OlapModel value);

} // VirtualCube
