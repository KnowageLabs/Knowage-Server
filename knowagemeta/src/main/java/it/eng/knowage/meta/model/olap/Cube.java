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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cube</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getModel <em>Model</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getTable <em>Table</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getDimensions <em>Dimensions</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getMeasures <em>Measures</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getCalculatedMembers <em>Calculated Members</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.Cube#getNamedSets <em>Named Sets</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube()
 * @model
 * @generated
 */
public interface Cube extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Model</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.OlapModel#getCubes <em>Cubes</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model</em>' container reference.
	 * @see #setModel(OlapModel)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_Model()
	 * @see it.eng.knowage.meta.model.olap.OlapModel#getCubes
	 * @model opposite="cubes" transient="false"
	 * @generated
	 */
	OlapModel getModel();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Cube#getModel <em>Model</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model</em>' container reference.
	 * @see #getModel()
	 * @generated
	 */
	void setModel(OlapModel value);

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
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_Table()
	 * @model required="true"
	 * @generated
	 */
	BusinessColumnSet getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Cube#getTable <em>Table</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table</em>' reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(BusinessColumnSet value);

	/**
	 * Returns the value of the '<em><b>Dimensions</b></em>' reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.olap.Dimension}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dimensions</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dimensions</em>' reference list.
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_Dimensions()
	 * @model required="true"
	 * @generated
	 */
	EList<Dimension> getDimensions();

	/**
	 * Returns the value of the '<em><b>Measures</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.olap.Measure}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.Measure#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Measures</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Measures</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_Measures()
	 * @see it.eng.knowage.meta.model.olap.Measure#getCube
	 * @model opposite="cube" containment="true" required="true"
	 * @generated
	 */
	EList<Measure> getMeasures();

	/**
	 * Returns the value of the '<em><b>Calculated Members</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.CalculatedMember#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calculated Members</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calculated Members</em>' reference.
	 * @see #setCalculatedMembers(CalculatedMember)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_CalculatedMembers()
	 * @see it.eng.knowage.meta.model.olap.CalculatedMember#getCube
	 * @model opposite="cube"
	 * @generated
	 */
	CalculatedMember getCalculatedMembers();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.Cube#getCalculatedMembers <em>Calculated Members</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calculated Members</em>' reference.
	 * @see #getCalculatedMembers()
	 * @generated
	 */
	void setCalculatedMembers(CalculatedMember value);

	/**
	 * Returns the value of the '<em><b>Named Sets</b></em>' containment reference list.
	 * The list contents are of type {@link it.eng.knowage.meta.model.olap.NamedSet}.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.NamedSet#getCube <em>Cube</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Named Sets</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Named Sets</em>' containment reference list.
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getCube_NamedSets()
	 * @see it.eng.knowage.meta.model.olap.NamedSet#getCube
	 * @model opposite="cube" containment="true"
	 * @generated
	 */
	EList<NamedSet> getNamedSets();

} // Cube
