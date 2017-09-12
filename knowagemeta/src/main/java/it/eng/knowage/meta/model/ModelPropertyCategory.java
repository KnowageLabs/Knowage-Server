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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Property Category</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.ModelPropertyCategory#getName <em>Name</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.ModelPropertyCategory#getParentCategory <em>Parent Category</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.ModelPropertyCategory#getSubCategories <em>Sub Categories</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.ModelPropertyCategory#getPropertyTypes <em>Property Types</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.ModelPropertyCategory#getDescription <em>Description</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory()
 * @model
 * @generated
 */
public interface ModelPropertyCategory extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.ModelPropertyCategory#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.ModelPropertyCategory#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Parent Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent Category</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Category</em>' reference.
	 * @see #setParentCategory(ModelPropertyCategory)
	 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory_ParentCategory()
	 * @model
	 * @generated
	 */
	ModelPropertyCategory getParentCategory();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.ModelPropertyCategory#getParentCategory <em>Parent Category</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Category</em>' reference.
	 * @see #getParentCategory()
	 * @generated
	 */
	void setParentCategory(ModelPropertyCategory value);

	/**
	 * Returns the value of the '<em><b>Sub Categories</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.ModelPropertyCategory}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sub Categories</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Categories</em>' reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory_SubCategories()
	 * @model
	 * @generated
	 */
	EList<ModelPropertyCategory> getSubCategories();

	/**
	 * Returns the value of the '<em><b>Property Types</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.ModelPropertyType}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.ModelPropertyType#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Types</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property Types</em>' reference list.
	 * @see it.eng.knowage.meta.model.ModelPackage#getModelPropertyCategory_PropertyTypes()
	 * @see it.eng.knowage.meta.model.ModelPropertyType#getCategory
	 * @model opposite="category"
	 * @generated
	 */
	EList<ModelPropertyType> getPropertyTypes();

} // ModelPropertyCategory
