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
package it.eng.qbe.statement;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Interface IStatement.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IStatement {

	public static final String OPERAND_TYPE_STATIC = "Static Content";
	public static final String OPERAND_TYPE_SUBQUERY = "Subquery";
	public static final String OPERAND_TYPE_SIMPLE_FIELD = "Field Content";
	public static final String OPERAND_TYPE_CALCULATED_FIELD = "calculated.field";
	public static final String OPERAND_TYPE_INLINE_CALCULATED_FIELD = "inline.calculated.field";
	public static final String OPERAND_TYPE_PARENT_FIELD = "Parent Field Content";

	public IDataSource getDataSource();

	public void setQuery(Query query);

	public Query getQuery();

	/*
	 * the number of the selected entities depends on the statement type and not
	 * on the abstract query
	 * 
	 * For example the following is the same query expressed in SQL and HQL ...
	 * 
	 * -> select f.unit_sales, p.brand_name from fact_sales f, product p where
	 * f.id_product = p.id_product
	 * 
	 * -> select f.unit_sales, f.product.brand_name from sales f
	 * 
	 * the first (SQL) have two selected entities the latter only 1
	 */
	public Set getSelectedEntities();

	public void prepare();

	public String getQueryString();

	public String getSqlQueryString();

	public int getOffset();

	public void setOffset(int offset);

	public int getFetchSize();

	public void setFetchSize(int fetchSize);

	public int getMaxResults();

	public void setMaxResults(int maxResults);

	public Map getParameters();

	public void setParameters(Map parameters);

	public Map getProfileAttributes();

	public void setProfileAttributes(Map profileAttributes);

	public String getNextAlias(Map entityAliasesMaps);

	// public String getFieldAlias(String rootEntityAlias, String queryName);

	public String getFieldAliasNoRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps);

	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String roleName);

	public List<String> getFieldAliasWithRolesList(IModelField datamartField, Map entityAliases, Map entityAliasesMaps);

	public String getFieldAliasWithRolesFromAlias(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String alias);

	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, IQueryField queryField);

	public String buildFromEntityAliasWithRoles(IModelEntity me, String rel, String entityAlias);

}
