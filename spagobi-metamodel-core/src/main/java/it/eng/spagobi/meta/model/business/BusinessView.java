/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Business View</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.business.BusinessView#getJoinRelationships <em>Join Relationships</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessView()
 * @model
 * @generated
 */
public interface BusinessView extends BusinessColumnSet {
	/**
	 * Returns the value of the '<em><b>Join Relationships</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.business.BusinessViewInnerJoinRelationship}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Join Relationships</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Join Relationships</em>' reference list.
	 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getBusinessView_JoinRelationships()
	 * @model
	 * @generated
	 */
	EList<BusinessViewInnerJoinRelationship> getJoinRelationships();
	
	// =========================================================================
	// Utility methods
	// =========================================================================
	List<PhysicalTable> getPhysicalTables();
	
	List<PhysicalTable> getPhysicalTablesOccurrences();
	
	//if the PhysicalTable has more occurrence, return the BusinessInnerJoinRelationship corresponding at the occurence numer specified
	BusinessViewInnerJoinRelationship getBusinessViewInnerJoinRelationshipAtOccurrenceNumber(PhysicalTable physicalTable, int index);

} // BusinessView
