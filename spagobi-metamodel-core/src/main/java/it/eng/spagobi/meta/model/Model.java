/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model;

import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.Model#getPhysicalModels <em>Physical Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.Model#getBusinessModels <em>Business Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.Model#getOlapModels <em>Olap Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.Model#getPropertyTypes <em>Property Types</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.Model#getPropertyCategories <em>Property Categories</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.ModelPackage#getModel()
 * @model
 * @generated
 */
public interface Model extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Physical Models</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.physical.PhysicalModel}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.physical.PhysicalModel#getParentModel <em>Parent Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Models</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Models</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.ModelPackage#getModel_PhysicalModels()
	 * @see it.eng.spagobi.meta.model.physical.PhysicalModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<PhysicalModel> getPhysicalModels();

	/**
	 * Returns the value of the '<em><b>Business Models</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.business.BusinessModel}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.business.BusinessModel#getParentModel <em>Parent Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Business Models</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Business Models</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.ModelPackage#getModel_BusinessModels()
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<BusinessModel> getBusinessModels();

	/**
	 * Returns the value of the '<em><b>Olap Models</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.OlapModel}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.OlapModel#getParentModel <em>Parent Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Olap Models</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Olap Models</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.ModelPackage#getModel_OlapModels()
	 * @see it.eng.spagobi.meta.model.olap.OlapModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<OlapModel> getOlapModels();

	/**
	 * Returns the value of the '<em><b>Property Types</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.ModelPropertyType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Types</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property Types</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.ModelPackage#getModel_PropertyTypes()
	 * @model containment="true"
	 * @generated
	 */
	EList<ModelPropertyType> getPropertyTypes();
	
	/**
	 * Returns the value of the '<em><b>Property Categories</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.ModelPropertyCategory}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Categories</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property Categories</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.ModelPackage#getModel_PropertyCategories()
	 * @model containment="true"
	 * @generated
	 */
	EList<ModelPropertyCategory> getPropertyCategories();

	// =========================================================================
	// Utility methods
	// =========================================================================
	
	ModelPropertyType getPropertyType(String name);
	
	ModelPropertyCategory getPropertyCategory(String name);
	
	DataSource getDataSource();

} // Model
