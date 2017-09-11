/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.olap;

import it.eng.spagobi.meta.model.ModelObject;

import it.eng.spagobi.meta.model.business.BusinessColumn;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Level</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getHierarchy <em>Hierarchy</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getColumn <em>Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getOrdinalColumn <em>Ordinal Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getNameColumn <em>Name Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getCaptionColumn <em>Caption Column</em>}</li>
 *   <li>{@link it.eng.spagobi.meta.model.olap.Level#getPropertyColumns <em>Property Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel()
 * @model
 * @generated
 */
public interface Level extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Hierarchy</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link it.eng.spagobi.meta.model.olap.Hierarchy#getLevels <em>Levels</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hierarchy</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hierarchy</em>' container reference.
	 * @see #setHierarchy(Hierarchy)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_Hierarchy()
	 * @see it.eng.spagobi.meta.model.olap.Hierarchy#getLevels
	 * @model opposite="levels" transient="false"
	 * @generated
	 */
	Hierarchy getHierarchy();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Level#getHierarchy <em>Hierarchy</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hierarchy</em>' container reference.
	 * @see #getHierarchy()
	 * @generated
	 */
	void setHierarchy(Hierarchy value);

	/**
	 * Returns the value of the '<em><b>Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Column</em>' reference.
	 * @see #setColumn(BusinessColumn)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_Column()
	 * @model
	 * @generated
	 */
	BusinessColumn getColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Level#getColumn <em>Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column</em>' reference.
	 * @see #getColumn()
	 * @generated
	 */
	void setColumn(BusinessColumn value);

	/**
	 * Returns the value of the '<em><b>Ordinal Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ordinal Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ordinal Column</em>' reference.
	 * @see #setOrdinalColumn(BusinessColumn)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_OrdinalColumn()
	 * @model
	 * @generated
	 */
	BusinessColumn getOrdinalColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Level#getOrdinalColumn <em>Ordinal Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ordinal Column</em>' reference.
	 * @see #getOrdinalColumn()
	 * @generated
	 */
	void setOrdinalColumn(BusinessColumn value);

	/**
	 * Returns the value of the '<em><b>Name Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name Column</em>' reference.
	 * @see #setNameColumn(BusinessColumn)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_NameColumn()
	 * @model
	 * @generated
	 */
	BusinessColumn getNameColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Level#getNameColumn <em>Name Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name Column</em>' reference.
	 * @see #getNameColumn()
	 * @generated
	 */
	void setNameColumn(BusinessColumn value);

	/**
	 * Returns the value of the '<em><b>Caption Column</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Caption Column</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Caption Column</em>' reference.
	 * @see #setCaptionColumn(BusinessColumn)
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_CaptionColumn()
	 * @model
	 * @generated
	 */
	BusinessColumn getCaptionColumn();

	/**
	 * Sets the value of the '{@link it.eng.spagobi.meta.model.olap.Level#getCaptionColumn <em>Caption Column</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Caption Column</em>' reference.
	 * @see #getCaptionColumn()
	 * @generated
	 */
	void setCaptionColumn(BusinessColumn value);

	/**
	 * Returns the value of the '<em><b>Property Columns</b></em>' reference list.
	 * The list contents are of type {@link it.eng.spagobi.meta.model.business.BusinessColumn}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property Columns</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property Columns</em>' reference list.
	 * @see it.eng.spagobi.meta.model.olap.OlapModelPackage#getLevel_PropertyColumns()
	 * @model
	 * @generated
	 */
	EList<BusinessColumn> getPropertyColumns();

} // Level
