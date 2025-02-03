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
package it.eng.knowage.meta.model;

import java.io.Serializable;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.olap.OlapModel;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Model</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.Model#getPhysicalModels <em>Physical Models</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.Model#getBusinessModels <em>Business Models</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.Model#getOlapModels <em>Olap Models</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.Model#getPropertyTypes <em>Property Types</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.Model#getPropertyCategories <em>Property Categories</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.ModelPackage#getModel()
 * @model
 * @generated
 */
public interface Model extends ModelObject, Serializable {
	/**
	 * Returns the value of the '<em><b>Physical Models</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.knowage.meta.model.physical.PhysicalModel}. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.physical.PhysicalModel#getParentModel <em>Parent Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Models</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Physical Models</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModel_PhysicalModels()
	 * @see it.eng.knowage.meta.model.physical.PhysicalModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<PhysicalModel> getPhysicalModels();

	/**
	 * Returns the value of the '<em><b>Business Models</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.knowage.meta.model.business.BusinessModel}. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.business.BusinessModel#getParentModel <em>Parent Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Business Models</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Business Models</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModel_BusinessModels()
	 * @see it.eng.knowage.meta.model.business.BusinessModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<BusinessModel> getBusinessModels();

	/**
	 * Returns the value of the '<em><b>Olap Models</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.knowage.meta.model.olap.OlapModel}. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.olap.OlapModel#getParentModel <em>Parent Model</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Olap Models</em>' containment reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Olap Models</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModel_OlapModels()
	 * @see it.eng.knowage.meta.model.olap.OlapModel#getParentModel
	 * @model opposite="parentModel" containment="true"
	 * @generated
	 */
	EList<OlapModel> getOlapModels();

	/**
	 * Returns the value of the '<em><b>Property Types</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.knowage.meta.model.ModelPropertyType}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Types</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Property Types</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModel_PropertyTypes()
	 * @model containment="true"
	 * @generated
	 */
	@Override
	EList<ModelPropertyType> getPropertyTypes();

	/**
	 * Returns the value of the '<em><b>Property Categories</b></em>' containment reference list. The list contents are of type
	 * {@link it.eng.knowage.meta.model.ModelPropertyCategory}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Categories</em>' containment reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Property Categories</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModel_PropertyCategories()
	 * @model containment="true"
	 * @generated
	 */
	EList<ModelPropertyCategory> getPropertyCategories();

	// =========================================================================
	// Utility methods
	// =========================================================================

	@Override
	ModelPropertyType getPropertyType(String name);

	ModelPropertyCategory getPropertyCategory(String name);

	IDataSource getDataSource();

	List<BusinessView> getBusinessViews();

} // Model
