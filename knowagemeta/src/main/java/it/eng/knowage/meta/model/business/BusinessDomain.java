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
package it.eng.knowage.meta.model.business;

import it.eng.knowage.meta.model.ModelObject;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business Domain</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessDomain#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessDomain#getTables <em>Tables</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessDomain#getRelationships <em>Relationships</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessDomain()
 * @model
 * @generated
 */
public interface BusinessDomain extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.business.BusinessModel#getDomains <em>Domains</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessDomain_Model()
	 * @see it.eng.knowage.meta.model.business.BusinessModel#getDomains
	 * @model opposite="domains" required="true" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessDomain#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

	/**
	 * Returns the value of the '<em><b>Tables</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.business.BusinessColumnSet}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tables</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tables</em>' reference list.
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessDomain_Tables()
	 * @model
	 * @generated
	 */
	EList<BusinessColumnSet> getTables();

	/**
	 * Returns the value of the '<em><b>Relationships</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.business.BusinessRelationship}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Relationships</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relationships</em>' reference list.
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessDomain_Relationships()
	 * @model
	 * @generated
	 */
	EList<BusinessRelationship> getRelationships();

} // BusinessDomain
