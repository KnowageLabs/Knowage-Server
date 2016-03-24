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
package it.eng.knowage.meta.model.physical;

import it.eng.knowage.meta.model.ModelObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Physical Column</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getComment <em>Comment</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDataType <em>Data Type</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getTypeName <em>Type Name</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getSize <em>Size</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getOctectLength <em>Octect Length</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDecimalDigits <em>Decimal Digits</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getRadix <em>Radix</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDefaultValue <em>Default Value</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#isNullable <em>Nullable</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getPosition <em>Position</em>}</li>
 * <li>{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getTable <em>Table</em>}</li>
 * </ul>
 * </p>
 * 
 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn()
 * @model
 * @generated
 */
public interface PhysicalColumn extends ModelObject {
	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getComment <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see #setDataType(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_DataType()
	 * @model
	 * @generated
	 */
	String getDataType();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDataType <em>Data Type</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Data Type</em>' attribute.
	 * @see #getDataType()
	 * @generated
	 */
	void setDataType(String value);

	/**
	 * Returns the value of the '<em><b>Type Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Type Name</em>' attribute.
	 * @see #setTypeName(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getTypeName <em>Type Name</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Type Name</em>' attribute.
	 * @see #getTypeName()
	 * @generated
	 */
	void setTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Size</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Size</em>' attribute.
	 * @see #setSize(int)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Size()
	 * @model
	 * @generated
	 */
	int getSize();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getSize <em>Size</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Size</em>' attribute.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(int value);

	/**
	 * Returns the value of the '<em><b>Octect Length</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Octect Length</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Octect Length</em>' attribute.
	 * @see #setOctectLength(int)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_OctectLength()
	 * @model
	 * @generated
	 */
	int getOctectLength();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getOctectLength <em>Octect Length</em>}' attribute. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Octect Length</em>' attribute.
	 * @see #getOctectLength()
	 * @generated
	 */
	void setOctectLength(int value);

	/**
	 * Returns the value of the '<em><b>Decimal Digits</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Decimal Digits</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Decimal Digits</em>' attribute.
	 * @see #setDecimalDigits(int)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_DecimalDigits()
	 * @model
	 * @generated
	 */
	int getDecimalDigits();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDecimalDigits <em>Decimal Digits</em>}' attribute. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Decimal Digits</em>' attribute.
	 * @see #getDecimalDigits()
	 * @generated
	 */
	void setDecimalDigits(int value);

	/**
	 * Returns the value of the '<em><b>Radix</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Radix</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Radix</em>' attribute.
	 * @see #setRadix(int)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Radix()
	 * @model
	 * @generated
	 */
	int getRadix();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getRadix <em>Radix</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Radix</em>' attribute.
	 * @see #getRadix()
	 * @generated
	 */
	void setRadix(int value);

	/**
	 * Returns the value of the '<em><b>Default Value</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Default Value</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Default Value</em>' attribute.
	 * @see #setDefaultValue(String)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_DefaultValue()
	 * @model
	 * @generated
	 */
	String getDefaultValue();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getDefaultValue <em>Default Value</em>}' attribute. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Default Value</em>' attribute.
	 * @see #getDefaultValue()
	 * @generated
	 */
	void setDefaultValue(String value);

	/**
	 * Returns the value of the '<em><b>Nullable</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nullable</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Nullable</em>' attribute.
	 * @see #setNullable(boolean)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Nullable()
	 * @model
	 * @generated
	 */
	boolean isNullable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#isNullable <em>Nullable</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Nullable</em>' attribute.
	 * @see #isNullable()
	 * @generated
	 */
	void setNullable(boolean value);

	/**
	 * Returns the value of the '<em><b>Position</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Position</em>' attribute.
	 * @see #setPosition(int)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Position()
	 * @model
	 * @generated
	 */
	int getPosition();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getPosition <em>Position</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Position</em>' attribute.
	 * @see #getPosition()
	 * @generated
	 */
	void setPosition(int value);

	/**
	 * Returns the value of the '<em><b>Table</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link it.eng.knowage.meta.model.physical.PhysicalTable#getColumns <em>Columns</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Table</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Table</em>' container reference.
	 * @see #setTable(PhysicalTable)
	 * @see it.eng.knowage.meta.model.physical.PhysicalModelPackage#getPhysicalColumn_Table()
	 * @see it.eng.knowage.meta.model.physical.PhysicalTable#getColumns
	 * @model opposite="columns" required="true" transient="false"
	 * @generated
	 */
	PhysicalTable getTable();

	/**
	 * Sets the value of the '{@link it.eng.knowage.meta.model.physical.PhysicalColumn#getTable <em>Table</em>}' container reference. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Table</em>' container reference.
	 * @see #getTable()
	 * @generated
	 */
	void setTable(PhysicalTable value);

	// =========================================================================
	// Utility methods
	// =========================================================================

	boolean isMarkedDeleted();

	boolean isPrimaryKey();

	boolean isPartOfCompositePrimaryKey();

} // PhysicalColumn
