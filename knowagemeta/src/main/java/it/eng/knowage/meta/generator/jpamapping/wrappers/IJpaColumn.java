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
package it.eng.knowage.meta.generator.jpamapping.wrappers;

public interface IJpaColumn {

	public static final String PUBLIC_SCOPE = "public"; //$NON-NLS-1$
	public static final String PROTECTED_SCOPE = "protected"; //$NON-NLS-1$
	public static final String PRIVATE_SCOPE = "private"; //$NON-NLS-1$

	/**
	 * 
	 * @return the parent table
	 */
	IJpaTable getJpaTable();

	String getName();

	String getDescription();

	/**
	 * 
	 * @return the phisical column name
	 */
	String getSqlName();

	String getUniqueName();

	/**
	 * 
	 * @return the phisical column name sourounded with double quote
	 */
	public abstract String getColumnNameDoubleQuoted();

	/**
	 * 
	 * @return true if the column is part of the primary key
	 */
	boolean isIdentifier();

	/**
	 * 
	 * @return true if the identifier is composite and we are writing the class identifier property
	 */
	boolean isPKReadOnly();

	/**
	 * 
	 * @return true if this Column belong to any relationship
	 */
	boolean isColumnInRelationship();

	/**
	 * 
	 * @return the generated bean property name for the given column (It does not return null)
	 */
	String getPropertyName();

	/**
	 * @return the sql type of the associated physical table
	 */
	String getSqlDataType();

	/**
	 * 
	 * @return the generated bean property name for the given column (ex. BigDecimal). It does not return null.
	 */
	String getSimplePropertyType();

	/**
	 * 
	 * @return the column type as java object (ex. java.math.BigDecimal). It does not return null.
	 */
	String getPropertyType();

	/**
	 * 
	 * @return the name of the 'get' method
	 */
	public abstract String getPropertyNameGetter();

	/**
	 * 
	 * @return the name of the 'set' method
	 */
	public abstract String getPropertyNameSetter();

	/**
	 * @return the value of the specified column attribute
	 */
	public String getAttribute(String name);

	/**
	 * @TODO da verificare
	 */
	boolean isDataTypeLOB();

	boolean needMapTemporalType();

	String getMapTemporalType();

	String getUnqualifiedUniqueName();

	/**
	 * Gets the name of the column for the jpa mapping file.. The name is quoted to avoid problems like upper cased characters
	 * 
	 * @return the name
	 */
	String getQuotedMappingColumnName();

	/**
	 * @return
	 */
	boolean isColumnInRelationshipWithView();

	boolean isDataTypeGeometry();

}