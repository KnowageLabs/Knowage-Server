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

import java.util.List;

import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dimension</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.olap.Dimension#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Dimension#getHierarchies <em>Hierarchies</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Dimension#getModel <em>Model</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getDimension()
 * @model
 * @generated
 */
public interface Dimension extends ModelObject {
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
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getDimension_Table()
	 * @model required="true"
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Dimension#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Hierarchies</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.olap.Hierarchy}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.Hierarchy#getDimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hierarchies</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hierarchies</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getDimension_Hierarchies()
	 * @see it.eng.knowage.meta.model.olap.Hierarchy#getDimension
	 * @model opposite="dimension" containment="true" required="true"
	 * @generated
	 */
	EList<Hierarchy> getHierarchies();

	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.OlapModel#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(OlapModel)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getDimension_Model()
	 * @see it.eng.knowage.meta.model.olap.OlapModel#getDimensions
	 * @model opposite="dimensions" transient="false"
	 * @generated
	 */
	OlapModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Dimension#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(OlapModel value);
	
	


} // Dimension
