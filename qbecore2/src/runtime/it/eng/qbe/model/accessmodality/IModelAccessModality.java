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
package it.eng.qbe.model.accessmodality;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.Filter;
import it.eng.qbe.query.Query;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IModelAccessModality {

	boolean isEntityAccessible(IModelEntity entity);

	/**
	 * Checks if is field accessible.
	 *
	 * @param tableName
	 *            the table name
	 * @param fieldName
	 *            the field name
	 *
	 * @return true, if is field accessible
	 */
	boolean isFieldAccessible(IModelField field);

	/**
	 * Gets the entity filter conditions.
	 *
	 * @param entityName
	 *            the entity name
	 *
	 * @return the entity filter conditions
	 */
	List<Filter> getEntityFilterConditions(String entityName);

	/**
	 * Gets the entity filter conditions.
	 *
	 * @param entityName
	 *            the entity name
	 * @param parameters
	 *            the parameters
	 *
	 * @return the entity filter conditions
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	List getEntityFilterConditions(String entityName, Properties parameters);

	public Boolean getRecursiveFiltering();

	public void setRecursiveFiltering(Boolean recursiveFiltering);

	public Query getFilteredStatement(Query query, IDataSource iDataSource, Map userProfileAttributes);
}
