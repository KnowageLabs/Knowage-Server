/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import java.util.Map;

import it.eng.spagobi.meta.model.ModelObject;

import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Hierarchy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Hierarchy#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Hierarchy#getLevels <em>Levels</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getHierarchy()
 * @model
 * @generated
 */
public interface Hierarchy extends ModelObject {
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
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getHierarchy_Table()
	 * @model
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Dimension</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Dimension#getHierarchies <em>Hierarchies</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimension</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dimension</em>' container reference.
	 * @see #setDimension(Dimension)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getHierarchy_Dimension()
	 * @see it.eng.spagobi.meta.model.olap.Dimension#getHierarchies
	 * @model opposite="hierarchies" transient="false"
	 * @generated
	 */
	Dimension getDimension();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dimension</em>' container reference.
	 * @see #getDimension()
	 * @generated
	 */
	void setDimension(Dimension value);

	/**
	 * Returns the value of the '<em><b>Levels</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.olap.Level}.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Level#getHierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Levels</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Levels</em>' containment reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getHierarchy_Levels()
	 * @see it.eng.spagobi.meta.model.olap.Level#getHierarchy
	 * @model opposite="hierarchy" containment="true" required="true"
	 * @generated
	 */
	EList<Level> getLevels();
	
	//Utility Methods ******************************** //

	IDataStore getMembers(String columnName);
	
	Map<Object, Object> getMembersMapBetweenLevels(String columnName1, String columnName2);
	
	IDataStore getSiblingValues(String siblingColumnName);
	
	Map<Object, Object> getMembersAndSibling(String levelName, String columnName);
	
} // Hierarchy
