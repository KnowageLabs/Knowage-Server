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
import it.eng.knowage.meta.model.physical.PhysicalColumn;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business Column Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessColumnSet#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.business.BusinessColumnSet#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessColumnSet()
 * @model
 * @generated
 */
public interface BusinessColumnSet extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.business.BusinessModel#getTables <em>Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessColumnSet_Model()
	 * @see it.eng.knowage.meta.model.business.BusinessModel#getTables
	 * @model opposite="tables" required="true" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.business.BusinessColumnSet#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.business.BusinessColumn}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.business.BusinessColumn#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.business.BusinessModelPackage#getBusinessColumnSet_Columns()
	 * @see it.eng.knowage.meta.model.business.BusinessColumn#getTable
	 * @model opposite="table" containment="true"
	 * @generated
	 */
	EList<BusinessColumn> getColumns();
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	
	BusinessIdentifier getIdentifier();
	
	/**
	 * Returns the <code>BusinessRelationship</code> objects for this table. 
	 */
	List<BusinessRelationship> getRelationships();
	
	SimpleBusinessColumn getSimpleBusinessColumnByUniqueName(String name);
	List<SimpleBusinessColumn> getSimpleBusinessColumnsByName(String name);
	List<SimpleBusinessColumn> getSimpleBusinessColumnsByPhysicalColumn(String physicalTableName, String physicalColumnName);
	List<SimpleBusinessColumn> getSimpleBusinessColumnsByPhysicalColumn(PhysicalColumn physicalColumn);
	
	/**
	 * @deprecated more than one column can have the same name
	 * @param name
	 * @return
	 */
	SimpleBusinessColumn getSimpleBusinessColumn(String name);
	
	/**
	 * @deprecated more than one column can be associated with the same physicalColumn
	 * @param name
	 * @return
	 */
	SimpleBusinessColumn getSimpleBusinessColumn(PhysicalColumn physicalColumn);
	
	CalculatedBusinessColumn getCalculatedBusinessColumn(String name);
	
	List<SimpleBusinessColumn> getSimpleBusinessColumns();
	List<CalculatedBusinessColumn> getCalculatedBusinessColumns();

} // BusinessColumnSet
