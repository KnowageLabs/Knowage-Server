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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Virtual Cube Measure</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getCube <em>Cube</em>}</li>
 *   <li>{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getMeasure <em>Measure</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure()
 * @model
 * @generated
 */
public interface VirtualCubeMeasure extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Virtual Cube</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link it.eng.knowage.meta.model.olap.VirtualCube#getMeasures <em>Measures</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Virtual Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Virtual Cube</em>' reference.
	 * @see #setVirtualCube(VirtualCube)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_VirtualCube()
	 * @see it.eng.knowage.meta.model.olap.VirtualCube#getMeasures
	 * @model opposite="measures"
	 * @generated
	 */
	VirtualCube getVirtualCube();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getVirtualCube <em>Virtual Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Virtual Cube</em>' reference.
	 * @see #getVirtualCube()
	 * @generated
	 */
	void setVirtualCube(VirtualCube value);

	/**
	 * Returns the value of the '<em><b>Cube</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cube</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cube</em>' reference.
	 * @see #setCube(Cube)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_Cube()
	 * @model
	 * @generated
	 */
	Cube getCube();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getCube <em>Cube</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cube</em>' reference.
	 * @see #getCube()
	 * @generated
	 */
	void setCube(Cube value);

	/**
	 * Returns the value of the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Measure</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Measure</em>' reference.
	 * @see #setMeasure(Measure)
	 * @see it.eng.knowage.meta.model.olap.OlapModelPackage#getVirtualCubeMeasure_Measure()
	 * @model
	 * @generated
	 */
	Measure getMeasure();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.olap.VirtualCubeMeasure#getMeasure <em>Measure</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Measure</em>' reference.
	 * @see #getMeasure()
	 * @generated
	 */
	void setMeasure(Measure value);

} // VirtualCubeMeasure
