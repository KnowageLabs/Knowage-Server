/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.impl;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.ModelPropertyCategory;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalModelPackage;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelImpl#getPhysicalModels <em>Physical Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelImpl#getBusinessModels <em>Business Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelImpl#getOlapModels <em>Olap Models</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelImpl#getPropertyTypes <em>Property Types</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelImpl#getPropertyCategories <em>Property Categories</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModelImpl extends ModelObjectImpl implements Model {
	/**
	 * The cached value of the '{@link #getPhysicalModels() <em>Physical Models</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPhysicalModels()
	 * @generated
	 * @ordered
	 */
	protected EList<PhysicalModel> physicalModels;

	/**
	 * The cached value of the '{@link #getBusinessModels() <em>Business Models</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBusinessModels()
	 * @generated
	 * @ordered
	 */
	protected EList<BusinessModel> businessModels;

	/**
	 * The cached value of the '{@link #getOlapModels() <em>Olap Models</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOlapModels()
	 * @generated
	 * @ordered
	 */
	protected EList<OlapModel> olapModels;

	/**
	 * The cached value of the '{@link #getPropertyTypes() <em>Property Types</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPropertyTypes()
	 * @generated
	 * @ordered
	 */
	protected EList<ModelPropertyType> propertyTypes;

	/**
	 * The cached value of the '{@link #getPropertyCategories() <em>Property Categories</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPropertyCategories()
	 * @generated
	 * @ordered
	 */
	protected EList<ModelPropertyCategory> propertyCategories;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<PhysicalModel> getPhysicalModels() {
		if (physicalModels == null) {
			physicalModels = new EObjectContainmentWithInverseEList<PhysicalModel>(PhysicalModel.class, this, ModelPackage.MODEL__PHYSICAL_MODELS, PhysicalModelPackage.PHYSICAL_MODEL__PARENT_MODEL);
		}
		return physicalModels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<BusinessModel> getBusinessModels() {
		if (businessModels == null) {
			businessModels = new EObjectContainmentWithInverseEList<BusinessModel>(BusinessModel.class, this, ModelPackage.MODEL__BUSINESS_MODELS, BusinessModelPackage.BUSINESS_MODEL__PARENT_MODEL);
		}
		return businessModels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<OlapModel> getOlapModels() {
		if (olapModels == null) {
			olapModels = new EObjectContainmentWithInverseEList<OlapModel>(OlapModel.class, this, ModelPackage.MODEL__OLAP_MODELS, OlapModelPackage.OLAP_MODEL__PARENT_MODEL);
		}
		return olapModels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ModelPropertyType> getPropertyTypes() {
		if (propertyTypes == null) {
			propertyTypes = new EObjectContainmentEList<ModelPropertyType>(ModelPropertyType.class, this, ModelPackage.MODEL__PROPERTY_TYPES);
		}
		return propertyTypes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ModelPropertyCategory> getPropertyCategories() {
		if (propertyCategories == null) {
			propertyCategories = new EObjectContainmentEList<ModelPropertyCategory>(ModelPropertyCategory.class, this, ModelPackage.MODEL__PROPERTY_CATEGORIES);
		}
		return propertyCategories;
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getPhysicalModels()).basicAdd(otherEnd, msgs);
			case ModelPackage.MODEL__BUSINESS_MODELS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getBusinessModels()).basicAdd(otherEnd, msgs);
			case ModelPackage.MODEL__OLAP_MODELS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOlapModels()).basicAdd(otherEnd, msgs);
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				return ((InternalEList<?>)getPhysicalModels()).basicRemove(otherEnd, msgs);
			case ModelPackage.MODEL__BUSINESS_MODELS:
				return ((InternalEList<?>)getBusinessModels()).basicRemove(otherEnd, msgs);
			case ModelPackage.MODEL__OLAP_MODELS:
				return ((InternalEList<?>)getOlapModels()).basicRemove(otherEnd, msgs);
			case ModelPackage.MODEL__PROPERTY_TYPES:
				return ((InternalEList<?>)getPropertyTypes()).basicRemove(otherEnd, msgs);
			case ModelPackage.MODEL__PROPERTY_CATEGORIES:
				return ((InternalEList<?>)getPropertyCategories()).basicRemove(otherEnd, msgs);
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				return getPhysicalModels();
			case ModelPackage.MODEL__BUSINESS_MODELS:
				return getBusinessModels();
			case ModelPackage.MODEL__OLAP_MODELS:
				return getOlapModels();
			case ModelPackage.MODEL__PROPERTY_TYPES:
				return getPropertyTypes();
			case ModelPackage.MODEL__PROPERTY_CATEGORIES:
				return getPropertyCategories();
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				getPhysicalModels().clear();
				getPhysicalModels().addAll((Collection<? extends PhysicalModel>)newValue);
				return;
			case ModelPackage.MODEL__BUSINESS_MODELS:
				getBusinessModels().clear();
				getBusinessModels().addAll((Collection<? extends BusinessModel>)newValue);
				return;
			case ModelPackage.MODEL__OLAP_MODELS:
				getOlapModels().clear();
				getOlapModels().addAll((Collection<? extends OlapModel>)newValue);
				return;
			case ModelPackage.MODEL__PROPERTY_TYPES:
				getPropertyTypes().clear();
				getPropertyTypes().addAll((Collection<? extends ModelPropertyType>)newValue);
				return;
			case ModelPackage.MODEL__PROPERTY_CATEGORIES:
				getPropertyCategories().clear();
				getPropertyCategories().addAll((Collection<? extends ModelPropertyCategory>)newValue);
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				getPhysicalModels().clear();
				return;
			case ModelPackage.MODEL__BUSINESS_MODELS:
				getBusinessModels().clear();
				return;
			case ModelPackage.MODEL__OLAP_MODELS:
				getOlapModels().clear();
				return;
			case ModelPackage.MODEL__PROPERTY_TYPES:
				getPropertyTypes().clear();
				return;
			case ModelPackage.MODEL__PROPERTY_CATEGORIES:
				getPropertyCategories().clear();
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
			case ModelPackage.MODEL__PHYSICAL_MODELS:
				return physicalModels != null && !physicalModels.isEmpty();
			case ModelPackage.MODEL__BUSINESS_MODELS:
				return businessModels != null && !businessModels.isEmpty();
			case ModelPackage.MODEL__OLAP_MODELS:
				return olapModels != null && !olapModels.isEmpty();
			case ModelPackage.MODEL__PROPERTY_TYPES:
				return propertyTypes != null && !propertyTypes.isEmpty();
			case ModelPackage.MODEL__PROPERTY_CATEGORIES:
				return propertyCategories != null && !propertyCategories.isEmpty();
		}
		return super.eIsSet(featureID);
	}


	public ModelPropertyCategory getPropertyCategory(String name) {
		
		for(int i = 0; i < getPropertyCategories().size(); i++) {
			if(getPropertyCategories().get(i).getName().equals(name)) {
				return getPropertyCategories().get(i);
			}
		}
		return null;
	}
	
	public DataSource getDataSource(){
		DataSource dataSource = new DataSource();
		dataSource.setLabel(getPropertyType("connection.name").getDefaultValue());
		dataSource.setUrlConnection(getPropertyType("connection.url").getDefaultValue());
		dataSource.setDriver(getPropertyType("connection.driver").getDefaultValue());
		dataSource.setUser(getPropertyType("connection.username").getDefaultValue());
		dataSource.setPwd(getPropertyType("connection.password").getDefaultValue());
		dataSource.setHibDialectClass("");
		dataSource.setHibDialectName("");
		return dataSource;
	}

} //ModelImpl
