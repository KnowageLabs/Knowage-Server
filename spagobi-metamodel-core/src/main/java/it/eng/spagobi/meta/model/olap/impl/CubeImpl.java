/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap.impl;

import it.eng.spagobi.meta.model.ModelPropertyType;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;

import it.eng.spagobi.meta.model.impl.ModelObjectImpl;

import it.eng.spagobi.meta.model.olap.CalculatedMember;
import it.eng.spagobi.meta.model.olap.Cube;
import it.eng.spagobi.meta.model.olap.Dimension;
import it.eng.spagobi.meta.model.olap.Measure;
import it.eng.spagobi.meta.model.olap.NamedSet;
import it.eng.spagobi.meta.model.olap.OlapModel;
import it.eng.spagobi.meta.model.olap.OlapModelPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cube</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getDimensions <em>Dimensions</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getMeasures <em>Measures</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getCalculatedMembers <em>Calculated Members</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.impl.CubeImpl#getNamedSets <em>Named Sets</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CubeImpl extends ModelObjectImpl implements Cube {
	/**
	 * The cached value of the '{@link #getTable() <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTable()
	 * @generated
	 * @ordered
	 */
	protected BusinessColumnSet table;

	/**
	 * The cached value of the '{@link #getDimensions() <em>Dimensions</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimensions()
	 * @generated
	 * @ordered
	 */
	protected EList<Dimension> dimensions;

	/**
	 * The cached value of the '{@link #getMeasures() <em>Measures</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMeasures()
	 * @generated
	 * @ordered
	 */
	protected EList<Measure> measures;

	/**
	 * The cached value of the '{@link #getCalculatedMembers() <em>Calculated Members</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalculatedMembers()
	 * @generated
	 * @ordered
	 */
	protected CalculatedMember calculatedMembers;

	/**
	 * The cached value of the '{@link #getNamedSets() <em>Named Sets</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamedSets()
	 * @generated
	 * @ordered
	 */
	protected EList<NamedSet> namedSets;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CubeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OlapModelPackage.Literals.CUBE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModel getModel() {
		if (eContainerFeatureID() != OlapModelPackage.CUBE__MODEL) return null;
		return (OlapModel)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetModel(OlapModel newModel, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newModel, OlapModelPackage.CUBE__MODEL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setModel(OlapModel newModel) {
		if (newModel != eInternalContainer() || (eContainerFeatureID() != OlapModelPackage.CUBE__MODEL && newModel != null)) {
			if (EcoreUtil.isAncestor(this, newModel))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newModel != null)
				msgs = ((InternalEObject)newModel).eInverseAdd(this, OlapModelPackage.OLAP_MODEL__CUBES, OlapModel.class, msgs);
			msgs = basicSetModel(newModel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.CUBE__MODEL, newModel, newModel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet getTable() {
		if (table != null && table.eIsProxy()) {
			InternalEObject oldTable = (InternalEObject)table;
			table = (BusinessColumnSet)eResolveProxy(oldTable);
			if (table != oldTable) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.CUBE__TABLE, oldTable, table));
			}
		}
		return table;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BusinessColumnSet basicGetTable() {
		return table;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTable(BusinessColumnSet newTable) {
		BusinessColumnSet oldTable = table;
		table = newTable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.CUBE__TABLE, oldTable, table));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Dimension> getDimensions() {
		if (dimensions == null) {
			dimensions = new EObjectResolvingEList<Dimension>(Dimension.class, this, OlapModelPackage.CUBE__DIMENSIONS);
		}
		return dimensions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Measure> getMeasures() {
		if (measures == null) {
			measures = new EObjectContainmentWithInverseEList<Measure>(Measure.class, this, OlapModelPackage.CUBE__MEASURES, OlapModelPackage.MEASURE__CUBE);
		}
		return measures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CalculatedMember getCalculatedMembers() {
		if (calculatedMembers != null && calculatedMembers.eIsProxy()) {
			InternalEObject oldCalculatedMembers = (InternalEObject)calculatedMembers;
			calculatedMembers = (CalculatedMember)eResolveProxy(oldCalculatedMembers);
			if (calculatedMembers != oldCalculatedMembers) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OlapModelPackage.CUBE__CALCULATED_MEMBERS, oldCalculatedMembers, calculatedMembers));
			}
		}
		return calculatedMembers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CalculatedMember basicGetCalculatedMembers() {
		return calculatedMembers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCalculatedMembers(CalculatedMember newCalculatedMembers, NotificationChain msgs) {
		CalculatedMember oldCalculatedMembers = calculatedMembers;
		calculatedMembers = newCalculatedMembers;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, OlapModelPackage.CUBE__CALCULATED_MEMBERS, oldCalculatedMembers, newCalculatedMembers);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCalculatedMembers(CalculatedMember newCalculatedMembers) {
		if (newCalculatedMembers != calculatedMembers) {
			NotificationChain msgs = null;
			if (calculatedMembers != null)
				msgs = ((InternalEObject)calculatedMembers).eInverseRemove(this, OlapModelPackage.CALCULATED_MEMBER__CUBE, CalculatedMember.class, msgs);
			if (newCalculatedMembers != null)
				msgs = ((InternalEObject)newCalculatedMembers).eInverseAdd(this, OlapModelPackage.CALCULATED_MEMBER__CUBE, CalculatedMember.class, msgs);
			msgs = basicSetCalculatedMembers(newCalculatedMembers, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OlapModelPackage.CUBE__CALCULATED_MEMBERS, newCalculatedMembers, newCalculatedMembers));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<NamedSet> getNamedSets() {
		if (namedSets == null) {
			namedSets = new EObjectContainmentWithInverseEList<NamedSet>(NamedSet.class, this, OlapModelPackage.CUBE__NAMED_SETS, OlapModelPackage.NAMED_SET__CUBE);
		}
		return namedSets;
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
			case OlapModelPackage.CUBE__MODEL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetModel((OlapModel)otherEnd, msgs);
			case OlapModelPackage.CUBE__MEASURES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getMeasures()).basicAdd(otherEnd, msgs);
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				if (calculatedMembers != null)
					msgs = ((InternalEObject)calculatedMembers).eInverseRemove(this, OlapModelPackage.CALCULATED_MEMBER__CUBE, CalculatedMember.class, msgs);
				return basicSetCalculatedMembers((CalculatedMember)otherEnd, msgs);
			case OlapModelPackage.CUBE__NAMED_SETS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getNamedSets()).basicAdd(otherEnd, msgs);
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
			case OlapModelPackage.CUBE__MODEL:
				return basicSetModel(null, msgs);
			case OlapModelPackage.CUBE__MEASURES:
				return ((InternalEList<?>)getMeasures()).basicRemove(otherEnd, msgs);
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				return basicSetCalculatedMembers(null, msgs);
			case OlapModelPackage.CUBE__NAMED_SETS:
				return ((InternalEList<?>)getNamedSets()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case OlapModelPackage.CUBE__MODEL:
				return eInternalContainer().eInverseRemove(this, OlapModelPackage.OLAP_MODEL__CUBES, OlapModel.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OlapModelPackage.CUBE__MODEL:
				return getModel();
			case OlapModelPackage.CUBE__TABLE:
				if (resolve) return getTable();
				return basicGetTable();
			case OlapModelPackage.CUBE__DIMENSIONS:
				return getDimensions();
			case OlapModelPackage.CUBE__MEASURES:
				return getMeasures();
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				if (resolve) return getCalculatedMembers();
				return basicGetCalculatedMembers();
			case OlapModelPackage.CUBE__NAMED_SETS:
				return getNamedSets();
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
			case OlapModelPackage.CUBE__MODEL:
				setModel((OlapModel)newValue);
				return;
			case OlapModelPackage.CUBE__TABLE:
				setTable((BusinessColumnSet)newValue);
				return;
			case OlapModelPackage.CUBE__DIMENSIONS:
				getDimensions().clear();
				getDimensions().addAll((Collection<? extends Dimension>)newValue);
				return;
			case OlapModelPackage.CUBE__MEASURES:
				getMeasures().clear();
				getMeasures().addAll((Collection<? extends Measure>)newValue);
				return;
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				setCalculatedMembers((CalculatedMember)newValue);
				return;
			case OlapModelPackage.CUBE__NAMED_SETS:
				getNamedSets().clear();
				getNamedSets().addAll((Collection<? extends NamedSet>)newValue);
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
			case OlapModelPackage.CUBE__MODEL:
				setModel((OlapModel)null);
				return;
			case OlapModelPackage.CUBE__TABLE:
				setTable((BusinessColumnSet)null);
				return;
			case OlapModelPackage.CUBE__DIMENSIONS:
				getDimensions().clear();
				return;
			case OlapModelPackage.CUBE__MEASURES:
				getMeasures().clear();
				return;
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				setCalculatedMembers((CalculatedMember)null);
				return;
			case OlapModelPackage.CUBE__NAMED_SETS:
				getNamedSets().clear();
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
			case OlapModelPackage.CUBE__MODEL:
				return getModel() != null;
			case OlapModelPackage.CUBE__TABLE:
				return table != null;
			case OlapModelPackage.CUBE__DIMENSIONS:
				return dimensions != null && !dimensions.isEmpty();
			case OlapModelPackage.CUBE__MEASURES:
				return measures != null && !measures.isEmpty();
			case OlapModelPackage.CUBE__CALCULATED_MEMBERS:
				return calculatedMembers != null;
			case OlapModelPackage.CUBE__NAMED_SETS:
				return namedSets != null && !namedSets.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.meta.model.ModelObject#getPropertyTypes()
	 */
	@Override
	public EList<ModelPropertyType> getPropertyTypes() {
		return getModel().getParentModel().getPropertyTypes();
	}

} //CubeImpl
