/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.generator.jpamapping.wrappers;

import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public interface IJpaTable {

	List<IJpaSubEntity> getSubEntities();

	/**
	 * 
	 * @return the package of the java class generated from this table
	 */
	String getPackage();

	/**
	 * 
	 * @return the string containing all the imports needed in order to successfully compile the java class generated from this table
	 */
	String getImportStatements();

	/**
	 * 
	 * @return the name of the table. It is used in java comments or for label generation
	 */
	String getName();

	/**
	 * 
	 * @return the name of the table. It is used in java comments or for tooltip generation
	 */
	String getDescription();

	/**
	 * 
	 * @return the name of the class generated from this table (not qualified)
	 */
	String getClassName();

	/**
	 * @returns the generated java class name (qualified).
	 */
	String getQualifiedClassName();

	String getSqlName();

	/**
	 * Gets the name of the table for the jpa mapping file.. The name is quoted to avoid problems like upper cased characters
	 * 
	 * @return the name
	 */
	String getQuotedMappingTableName();

	String getUniqueName();

	String getCatalog();

	String getSchema();

	/**
	 * 
	 * @return return true if the wrapped table has no key
	 */
	boolean hasFakePrimaryKey();

	/**
	 * 
	 * @return it return false iff the table have one column key. If the table have no key it return true in order to generate a fake key composed by all
	 *         columns contained in the table just to keep happy the jpa runtime that work only if all entities have a key. To understand if the composed key is
	 *         a real key or a fake one use the method <code>hasFakePrimaryKey</code>
	 */
	boolean hasCompositeKey();

	/**
	 * 
	 * @return the name of the java class used to map the composite primary key (note: composite primary key are mapped in a separate class and not inline in
	 *         the same class of the table they belong to)
	 */
	String getCompositeKeyClassName();

	String getQualifiedCompositeKeyClassName();

	/**
	 * 
	 * @return the name of the composed key property
	 */
	String getCompositeKeyPropertyName();

	/**
	 * 
	 * @return the columns contained in the primary key
	 */
	List<IJpaColumn> getPrimaryKeyColumns();

	/**
	 * 
	 * @return the default fetch strategy
	 */
	String getDefaultFetch();

	/**
	 * 
	 * @return all the columns contained in this business table
	 */
	List<IJpaColumn> getColumns();

	/**
	 * 
	 * @return equals to getSimpleColumns(true, true, true);
	 */
	List<IJpaColumn> getSimpleColumns();

	/**
	 * Returns the <code>IJpaColumn</code> objects for the the columns that are not part of any association.
	 * 
	 * @param genOnly
	 *            Whether to include only the columns marked for generation.
	 * 
	 * @param includePk
	 *            Whether to include the primary key column(s).
	 * 
	 * @param includeInherited
	 *            Whether to include the columns associated with java properties that exist in the super class (if any).
	 */
	List<IJpaColumn> getSimpleColumns(boolean genOnly, boolean includePk, boolean includeInherited);

	List<IJpaCalculatedColumn> getCalculatedColumns();

	/**
	 * 
	 * @return all the relationships defined upon this table
	 */
	List<IJpaRelationship> getRelationships();

	/**
	 * @return the value of the specified table attribute
	 */
	public String getAttribute(String name);

	/**
	 * @return true only if the table if this table is used inside a BusinessView
	 */
	public boolean isInnerTable();

	/**
	 * 
	 * @return the string corresponding to the physical table type (Table, View)
	 */
	public String getPhysicalType();

	/**
	 * @return the value of the business model property useSchema, if "true" the schema name will be included in the mapping (if it has a value)
	 */
	public String getUseSchema();

	/**
	 * @return the value of the business model property useCatalog, if "true" the catalog name will be included in the mapping (if it has a value)
	 */
	public String getUseCatalog();
}
