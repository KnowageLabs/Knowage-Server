/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.impl;

import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.ModelPropertyCategory;
import it.eng.spagobi.meta.model.ModelPropertyType;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Property Category</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl#getName <em>Name</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl#getParentCategory <em>Parent Category</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl#getSubCategories <em>Sub Categories</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl#getPropertyTypes <em>Property Types</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelPropertyCategoryImpl#getDescription <em>Description</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModelPropertyCategoryImpl extends EObjectImpl implements ModelPropertyCategory {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParentCategory() <em>Parent Category</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentCategory()
	 * @generated
	 * @ordered
	 */
	protected ModelPropertyCategory parentCategory;

	/**
	 * The cached value of the '{@link #getSubCategories() <em>Sub Categories</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubCategories()
	 * @generated
	 * @ordered
	 */
	protected EList<ModelPropertyCategory> subCategories;

	/**
	 * The cached value of the '{@link #getPropertyTypes() <em>Property Types</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPropertyTypes()
	 * @generated
	 * @ordered
	 */
	protected EList<ModelPropertyType> propertyTypes;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelPropertyCategoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.MODEL_PROPERTY_CATEGORY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_PROPERTY_CATEGORY__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_PROPERTY_CATEGORY__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelPropertyCategory getParentCategory() {
		if (parentCategory != null && parentCategory.eIsProxy()) {
			InternalEObject oldParentCategory = (InternalEObject)parentCategory;
			parentCategory = (ModelPropertyCategory)eResolveProxy(oldParentCategory);
			if (parentCategory != oldParentCategory) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY, oldParentCategory, parentCategory));
			}
		}
		return parentCategory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelPropertyCategory basicGetParentCategory() {
		return parentCategory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentCategory(ModelPropertyCategory newParentCategory) {
		ModelPropertyCategory oldParentCategory = parentCategory;
		parentCategory = newParentCategory;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY, oldParentCategory, parentCategory));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ModelPropertyCategory> getSubCategories() {
		if (subCategories == null) {
			subCategories = new EObjectResolvingEList<ModelPropertyCategory>(ModelPropertyCategory.class, this, ModelPackage.MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES);
		}
		return subCategories;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ModelPropertyType> getPropertyTypes() {
		if (propertyTypes == null) {
			propertyTypes = new EObjectWithInverseResolvingEList<ModelPropertyType>(ModelPropertyType.class, this, ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES, ModelPackage.MODEL_PROPERTY_TYPE__CATEGORY);
		}
		return propertyTypes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getPropertyTypes()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				return ((InternalEList<?>)getPropertyTypes()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__NAME:
				return getName();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY:
				if (resolve) return getParentCategory();
				return basicGetParentCategory();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES:
				return getSubCategories();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				return getPropertyTypes();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__DESCRIPTION:
				return getDescription();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__NAME:
				setName((String)newValue);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY:
				setParentCategory((ModelPropertyCategory)newValue);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES:
				getSubCategories().clear();
				getSubCategories().addAll((Collection<? extends ModelPropertyCategory>)newValue);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				getPropertyTypes().clear();
				getPropertyTypes().addAll((Collection<? extends ModelPropertyType>)newValue);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__DESCRIPTION:
				setDescription((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY:
				setParentCategory((ModelPropertyCategory)null);
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES:
				getSubCategories().clear();
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				getPropertyTypes().clear();
				return;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.MODEL_PROPERTY_CATEGORY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PARENT_CATEGORY:
				return parentCategory != null;
			case ModelPackage.MODEL_PROPERTY_CATEGORY__SUB_CATEGORIES:
				return subCategories != null && !subCategories.isEmpty();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__PROPERTY_TYPES:
				return propertyTypes != null && !propertyTypes.isEmpty();
			case ModelPackage.MODEL_PROPERTY_CATEGORY__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //ModelPropertyCategoryImpl
