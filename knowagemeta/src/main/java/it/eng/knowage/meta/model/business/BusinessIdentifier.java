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
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business Identifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getColumns <em>Columns</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey <em>Physical Primary Key</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessIdentifier()
 * @model
 * @generated
 */
public interface BusinessIdentifier extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.business.BusinessModel#getIdentifiers <em>Identifiers</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Model()
	 * @see it.eng.knowage.meta.model.business.BusinessModel#getIdentifiers
	 * @model opposite="identifiers" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

	/**
	 * Returns the value of the '<em><b>Table</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Table</em>' reference.
	 * @see #setTable(BusinessColumnSet)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Table()
	 * @model required="true"
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.business.BusinessColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' reference list.
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessIdentifier_Columns()
	 * @model required="true"
	 * @generated
	 */
	EList<BusinessColumn> getColumns();

	/**
	 * Returns the value of the '<em><b>Physical Primary Key</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Primary Key</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Primary Key</em>' reference.
	 * @see #setPhysicalPrimaryKey(PhysicalPrimaryKey)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessIdentifier_PhysicalPrimaryKey()
	 * @model
	 * @generated
	 */
	PhysicalPrimaryKey getPhysicalPrimaryKey();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessIdentifier#getPhysicalPrimaryKey <em>Physical Primary Key</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Primary Key</em>' reference.
	 * @see #getPhysicalPrimaryKey()
	 * @generated
	 */
	void setPhysicalPrimaryKey(PhysicalPrimaryKey value);
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	
	List<SimpleBusinessColumn> getSimpleBusinessColumns();

} // BusinessIdentifier
