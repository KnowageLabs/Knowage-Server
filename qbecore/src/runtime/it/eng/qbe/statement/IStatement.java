/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
