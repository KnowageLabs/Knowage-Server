/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;

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
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getColumns <em>Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessColumnSet()
 * @model
 * @generated
 */
public interface BusinessColumnSet extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.business.BusinessModel#getTables <em>Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(BusinessModel)
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessColumnSet_Model()
	 * @see it.eng.spagobi.meta.model.business.BusinessModel#getTables
	 * @model opposite="tables" required="true" transient="false"
	 * @generated
	 */
	BusinessModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.business.BusinessColumnSet#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(BusinessModel value);

	/**
	 * Returns the value of the '<em><b>Columns</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.business.BusinessColumn}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.business.BusinessColumn#getTable <em>Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Columns</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Columns</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessColumnSet_Columns()
	 * @see it.eng.spagobi.meta.model.business.BusinessColumn#getTable
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
