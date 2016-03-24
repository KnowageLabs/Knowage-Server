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
package it.eng.knowage.meta.model.olap;

import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Hierarchy</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.olap.Hierarchy#getTable <em>Table</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.olap.Hierarchy#getLevels <em>Levels</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getHierarchy()
 * @model
 * @generated
 */
public interface Hierarchy extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Table</b></em>' reference. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Table</em>' reference.
	 * @see #setTable(BusinessColumnSet)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getHierarchy_Table()
	 * @model
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Hierarchy#getTable <em>Table</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Dimension</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.olap.Dimension#getHierarchies <em>Hierarchies</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimension</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Dimension</em>' container reference.
	 * @see #setDimension(Dimension)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getHierarchy_Dimension()
	 * @see it.eng.knowage.meta.model.olap.Dimension#getHierarchies
	 * @model opposite="hierarchies" transient="false"
	 * @generated
	 */
	Dimension getDimension();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}' container reference. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Dimension</em>' container reference.
	 * @see #getDimension()
	 * @generated
	 */
	void setDimension(Dimension value);

	/**
	 * Returns the value of the '<em><b>Levels</b></em>' containment reference list. The list contents are of type {@link it.eng.knowage.meta.model.olap.Level}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.Level#getHierarchy <em>Hierarchy</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Levels</em>' reference list isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Levels</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getHierarchy_Levels()
	 * @see it.eng.knowage.meta.model.olap.Level#getHierarchy
	 * @model opposite="hierarchy" containment="true" required="true"
	 * @generated
	 */
	EList<Level> getLevels();

	// Utility Methods ******************************** //

	IDataStore getMembers(String columnName);

	Map<Object, Object> getMembersMapBetweenLevels(String columnName1, String columnName2);

	IDataStore getSiblingValues(String siblingColumnName);

	Map<Object, Object> getMembersAndSibling(String levelName, String columnName);

} // Hierarchy
