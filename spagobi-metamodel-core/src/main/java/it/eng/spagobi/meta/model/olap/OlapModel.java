/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelObject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Olap Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.OlapModel#getParentModel <em>Parent Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.OlapModel#getCubes <em>Cubes</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.OlapModel#getVirtualCubes <em>Virtual Cubes</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.OlapModel#getDimensions <em>Dimensions</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getOlapModel()
 * @model
 * @generated
 */
public interface OlapModel extends ModelObject {

	/**
	 * Returns the value of the '<em><b>Parent Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.Model#getOlapModels <em>Olap Models</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Model</em>' container reference.
	 * @see #setParentModel(Model)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getOlapModel_ParentModel()
	 * @see it.eng.spagobi.meta.model.Model#getOlapModels
	 * @model opposite="olapModels" required="true" transient="false"
	 * @generated
	 */
	Model getParentModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.OlapModel#getParentModel <em>Parent Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Model</em>' container reference.
	 * @see #getParentModel()
	 * @generated
	 */
	void setParentModel(Model value);

	/**
	 * Returns the value of the '<em><b>Cubes</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.Cube}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Cube#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cubes</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cubes</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getOlapModel_Cubes()
	 * @see it.eng.spagobi.meta.model.olap.Cube#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<Cube> getCubes();

	/**
	 * Returns the value of the '<em><b>Virtual Cubes</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.VirtualCube}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.VirtualCube#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Virtual Cubes</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Virtual Cubes</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getOlapModel_VirtualCubes()
	 * @see it.eng.spagobi.meta.model.olap.VirtualCube#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<VirtualCube> getVirtualCubes();

	/**
	 * Returns the value of the '<em><b>Dimensions</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.Dimension}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Dimension#getModel <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimensions</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dimensions</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getOlapModel_Dimensions()
	 * @see it.eng.spagobi.meta.model.olap.Dimension#getModel
	 * @model opposite="model" containment="true"
	 * @generated
	 */
	EList<Dimension> getDimensions();
} // OlapModel
