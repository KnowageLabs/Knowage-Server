/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.impl;

import it.eng.spagobi.meta.model.ModelFactory;
import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.ModelPackage;
import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.exception.ModelObjectNotFoundException;
import it.eng.spagobi.meta.model.exception.ModelRuntimeException;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Object</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl#getId <em>Id</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl#getName <em>Name</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl#getUniqueName <em>Unique Name</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.impl.ModelObjectImpl#getProperties <em>Properties</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ModelObjectImpl extends EObjectImpl implements ModelObject {

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;
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
	 * The default value of the '{@link #getUniqueName() <em>Unique Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUniqueName()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIQUE_NAME_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getUniqueName() <em>Unique Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUniqueName()
	 * @generated
	 * @ordered
	 */
	protected String uniqueName = UNIQUE_NAME_EDEFAULT;
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
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, ModelProperty> properties;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelObjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.MODEL_OBJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_OBJECT__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_OBJECT__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUniqueName(String newUniqueName) {
		String oldUniqueName = uniqueName;
		uniqueName = newUniqueName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_OBJECT__UNIQUE_NAME, oldUniqueName, uniqueName));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MODEL_OBJECT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, ModelProperty> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,ModelProperty>(ModelPackage.Literals.MODEL_PROPERTY_MAP_ENTRY, ModelPropertyMapEntryImpl.class, this, ModelPackage.MODEL_OBJECT__PROPERTIES);
		}
		return properties;
	}

	

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.MODEL_OBJECT__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
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
			case ModelPackage.MODEL_OBJECT__ID:
				return getId();
			case ModelPackage.MODEL_OBJECT__NAME:
				return getName();
			case ModelPackage.MODEL_OBJECT__UNIQUE_NAME:
				return getUniqueName();
			case ModelPackage.MODEL_OBJECT__DESCRIPTION:
				return getDescription();
			case ModelPackage.MODEL_OBJECT__PROPERTIES:
				if (coreType) return getProperties();
				else return getProperties().map();
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
			case ModelPackage.MODEL_OBJECT__ID:
				setId((String)newValue);
				return;
			case ModelPackage.MODEL_OBJECT__NAME:
				setName((String)newValue);
				return;
			case ModelPackage.MODEL_OBJECT__UNIQUE_NAME:
				setUniqueName((String)newValue);
				return;
			case ModelPackage.MODEL_OBJECT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case ModelPackage.MODEL_OBJECT__PROPERTIES:
				((EStructuralFeature.Setting)getProperties()).set(newValue);
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
			case ModelPackage.MODEL_OBJECT__ID:
				setId(ID_EDEFAULT);
				return;
			case ModelPackage.MODEL_OBJECT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelPackage.MODEL_OBJECT__UNIQUE_NAME:
				setUniqueName(UNIQUE_NAME_EDEFAULT);
				return;
			case ModelPackage.MODEL_OBJECT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case ModelPackage.MODEL_OBJECT__PROPERTIES:
				getProperties().clear();
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
			case ModelPackage.MODEL_OBJECT__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ModelPackage.MODEL_OBJECT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelPackage.MODEL_OBJECT__UNIQUE_NAME:
				return UNIQUE_NAME_EDEFAULT == null ? uniqueName != null : !UNIQUE_NAME_EDEFAULT.equals(uniqueName);
			case ModelPackage.MODEL_OBJECT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case ModelPackage.MODEL_OBJECT__PROPERTIES:
				return properties != null && !properties.isEmpty();
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
		result.append(" (id: ");
		result.append(id);
		result.append(", name: ");
		result.append(name);
		result.append(", uniqueName: ");
		result.append(uniqueName);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

	
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	
	
	@Override
	public String setProperty(ModelPropertyType propertyType, String value) {
		ModelProperty property;
		String oldValue;
		
		oldValue = null;
		
		try {
			assert propertyType != null : "input parameter property type cannot be null";
			
			property = getProperties().get( propertyType.getId() );
			if(property == null) {
				property = ModelFactory.eINSTANCE.createModelProperty();
				property.setPropertyType(propertyType);
			} else {
				oldValue = property.getValue();
			}
					
			property.setValue(value);
			getProperties().put(propertyType.getId(), property);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to set property of model object [" + getName() +"]");
		} finally {
			
		}
		
		return oldValue;
	}
	
	public String setProperty(String pname, String pvalue) {
		String oldValue;
		ModelPropertyType propertyType;
		
		propertyType = null;
		oldValue = null;
		
		try {
			assert pname != null : "Input parameter pname cannot be null";
			
			propertyType = getPropertyType(pname);
			
			if(propertyType == null) {
				throw new ModelObjectNotFoundException("Impossible to find a property-type named [" + pname + "] in the model");
			}
			
			oldValue = setProperty(propertyType, pvalue);
		} catch (Throwable t) {
			throw new ModelRuntimeException("Impossible to set property of business column [" + getName() + "]", t);
		} finally {
			
		}
		
		return oldValue;
	}
	
	@Override
	public ModelPropertyType getPropertyType(String id) {
		for(int i = 0; i < getPropertyTypes().size(); i++) {
			if(getPropertyTypes().get(i).getId().equals(id)) {
				return getPropertyTypes().get(i);
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelObjectImpl other = (ModelObjectImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	


} //ModelObjectImpl
